package me.june8th.ticketrushserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.contents.SearchDocument;
import me.june8th.ticketrushserver.database.EventRepository;
import me.june8th.ticketrushserver.database.OrganizationAccountRepository;
import me.june8th.ticketrushserver.contents.SearchDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_LIMIT = 50;

    private final SearchDocumentRepository searchDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final EventRepository eventRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void reindexSearchDocuments() {
        organizationAccountRepository.findAll().forEach(this::indexOrganization);
        eventRepository.findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(Instant.now()).forEach(this::indexEvent);
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(String query, int limit) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isBlank()) return List.of();

        int normalizedLimit = Math.clamp(limit, 1, MAX_LIMIT);
        Instant now = Instant.now();
        StringQuery searchQuery = new StringQuery(buildSearchQuery(normalizedQuery, now));
        searchQuery.setPageable(PageRequest.of(0, normalizedLimit));

        List<SearchDocument> hits = elasticsearchOperations.search(searchQuery, SearchDocument.class).stream()
                .map(SearchHit::getContent)
                .toList();
        return hydrateResults(hits, now);
    }

    public void indexEvent(Event event) {
        if (event == null) return;
        try {
            if (!event.getPublished()) {
                searchDocumentRepository.deleteById(SearchDocument.eventId(event.getId()));
                return;
            }
            searchDocumentRepository.save(SearchDocument.builder()
                    .id(SearchDocument.eventId(event.getId()))
                    .type(SearchDocument.TYPE_EVENT)
                    .sourceId(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .published(event.getPublished())
                    .dateTime(event.getDateTime())
                    .build());
        } catch (RuntimeException e) {
            log.warn("Failed to index event {}: {}", event.getId(), e.getMessage());
        }
    }

    public void deleteEvent(long eventId) {
        try {
            searchDocumentRepository.deleteById(SearchDocument.eventId(eventId));
        } catch (RuntimeException e) {
            log.warn("Failed to delete event {} from search index: {}", eventId, e.getMessage());
        }
    }

    public void indexOrganization(OrganizationAccount organization) {
        if (organization == null) return;
        try {
            searchDocumentRepository.save(SearchDocument.builder()
                    .id(SearchDocument.organizationId(organization.getId()))
                    .type(SearchDocument.TYPE_ORGANIZATION)
                    .sourceId(organization.getId())
                    .name(organization.getName())
                    .description(organization.getDescription())
                    .verified(organization.getVerified())
                    .build());
        } catch (RuntimeException e) {
            log.warn("Failed to index organization {}: {}", organization.getId(), e.getMessage());
        }
    }

    public void deleteOrganization(long organizationId) {
        try {
            searchDocumentRepository.deleteById(SearchDocument.organizationId(organizationId));
        } catch (RuntimeException e) {
            log.warn("Failed to delete organization {} from search index: {}", organizationId, e.getMessage());
        }
    }

    private List<SearchResult> hydrateResults(List<SearchDocument> hits, Instant now) {
        Set<Long> eventIds = new LinkedHashSet<>();
        Set<Long> organizationIds = new LinkedHashSet<>();
        for (SearchDocument hit : hits) {
            if (SearchDocument.TYPE_EVENT.equals(hit.getType())) {
                eventIds.add(hit.getSourceId());
            } else if (SearchDocument.TYPE_ORGANIZATION.equals(hit.getType())) {
                organizationIds.add(hit.getSourceId());
            }
        }

        Map<Long, Event> events = eventRepository.findAllById(eventIds).stream()
                .filter(event -> event.getPublished() && event.getDateTime().isAfter(now))
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        Map<Long, OrganizationAccount> organizations = organizationAccountRepository.findAllById(organizationIds).stream()
                .collect(Collectors.toMap(OrganizationAccount::getId, Function.identity()));

        return hits.stream()
                .map(hit -> toSearchResult(hit, events, organizations))
                .filter(Objects::nonNull)
                .toList();
    }

    private SearchResult toSearchResult(SearchDocument hit, Map<Long, Event> events, Map<Long, OrganizationAccount> organizations) {
        if (SearchDocument.TYPE_EVENT.equals(hit.getType())) {
            Event event = events.get(hit.getSourceId());
            if (event == null) return null;
            return new SearchResult(
                    event.getId(),
                    SearchDocument.TYPE_EVENT,
                    event.getName(),
                    storageService.generatePresignedUrl(event.getBannerKey()),
                    event.getVenue(),
                    null,
                    null
            );
        }
        if (SearchDocument.TYPE_ORGANIZATION.equals(hit.getType())) {
            OrganizationAccount organization = organizations.get(hit.getSourceId());
            if (organization == null) return null;
            return new SearchResult(
                    organization.getId(),
                    SearchDocument.TYPE_ORGANIZATION,
                    organization.getName(),
                    null,
                    null,
                    storageService.generatePresignedUrl(organization.getAvatarKey()),
                    organization.getVerified()
            );
        }
        return null;
    }

    private String buildSearchQuery(String query, Instant now) {
        String queryValue = json(query);
        String nowValue = json(now.toString());
        return """
                {
                  "bool": {
                    "must": [
                      {
                        "bool": {
                          "should": [
                            {
                              "multi_match": {
                                "query": %s,
                                "fields": ["name^4", "description"],
                                "fuzziness": "AUTO"
                              }
                            },
                            {
                              "match_phrase_prefix": {
                                "name": {
                                  "query": %s,
                                  "boost": 3
                                }
                              }
                            },
                            {
                              "match_phrase_prefix": {
                                "description": {
                                  "query": %s
                                }
                              }
                            }
                          ],
                          "minimum_should_match": 1
                        }
                      }
                    ],
                    "filter": [
                      {
                        "bool": {
                          "should": [
                            {
                              "term": {
                                "type": "ORGANIZATION"
                              }
                            },
                            {
                              "bool": {
                                "filter": [
                                  {
                                    "term": {
                                      "type": "EVENT"
                                    }
                                  },
                                  {
                                    "term": {
                                      "published": true
                                    }
                                  },
                                  {
                                    "range": {
                                      "dateTime": {
                                        "gt": %s
                                      }
                                    }
                                  }
                                ]
                              }
                            }
                          ],
                          "minimum_should_match": 1
                        }
                      }
                    ]
                  }
                }
                """.formatted(queryValue, queryValue, queryValue, nowValue);
    }

    private String json(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid search query", e);
        }
    }

    public record SearchResult(
            long id,
            String type,
            String name,
            String bannerUrl,
            String venue,
            String avatarUrl,
            Boolean verified
    ) {}

}
