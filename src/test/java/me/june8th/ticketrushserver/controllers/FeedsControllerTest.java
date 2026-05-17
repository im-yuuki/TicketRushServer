package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.services.EventService;
import me.june8th.ticketrushserver.services.FeedService;
import me.june8th.ticketrushserver.services.SearchService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.support.TestAuthenticatedAccount;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedsControllerTest {

    @Mock
    private FeedService feedService;

    @Mock
    private SearchService searchService;

    @Mock
    private StorageService storageService;

    @Mock
    private EventService eventService;

    @Mock
    private ClientIPResolver clientIPResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FeedsController(feedService, searchService, storageService, eventService))
                .setControllerAdvice(new ErrorHandler(clientIPResolver))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void promoted_shouldReturnFeedItems() throws Exception {
        Event event = createEvent(1L, "Promoted Event");
        when(feedService.getPromotedEvents()).thenReturn(List.of(event));
        when(eventService.getMinimumTicketPrices(List.of(event))).thenReturn(Map.of(1L, 120000L));
        when(storageService.generatePresignedUrl("events/1.png")).thenReturn("https://cdn.example.com/events/1.png");

        mockMvc.perform(get("/feeds/promoted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Promoted Event"))
                .andExpect(jsonPath("$[0].bannerUrl").value("https://cdn.example.com/events/1.png"))
                .andExpect(jsonPath("$[0].minimumTicketPrice").value(120000));

        verify(feedService).getPromotedEvents();
    }

    @Test
    void trending_shouldReturnFeedItems() throws Exception {
        Event event = createEvent(2L, "Trending Event");
        when(feedService.getTrendingEvents()).thenReturn(List.of(event));
        when(eventService.getMinimumTicketPrices(List.of(event))).thenReturn(Map.of(2L, 90000L));
        when(storageService.generatePresignedUrl("events/2.png")).thenReturn("https://cdn.example.com/events/2.png");

        mockMvc.perform(get("/feeds/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Trending Event"))
                .andExpect(jsonPath("$[0].minimumTicketPrice").value(90000));

        verify(feedService).getTrendingEvents();
    }

    @Test
    void recommendeds_shouldSupportAnonymousRequests() throws Exception {
        Event event = createEvent(3L, "Recommended Event");
        when(feedService.getRecommendedEvents(null)).thenReturn(List.of(event));
        when(eventService.getMinimumTicketPrices(List.of(event))).thenReturn(Map.of(3L, 70000L));
        when(storageService.generatePresignedUrl("events/3.png")).thenReturn("https://cdn.example.com/events/3.png");

        mockMvc.perform(get("/feeds/recommendeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].minimumTicketPrice").value(70000));

        verify(feedService).getRecommendedEvents(null);
    }

    @Test
    void recommendeds_shouldPassAuthenticatedPrincipalToService() throws Exception {
        TestAuthenticatedAccount account = TestAuthenticatedAccount.builder()
                .id(42L)
                .name("Test User")
                .email("test.user@example.com")
                .role(Role.USER)
                .domain("42")
                .tokenVersion(0)
                .build();
        Event event = createEvent(4L, "Personalized Event");

        when(feedService.getRecommendedEvents(42L)).thenReturn(List.of(event));
        when(eventService.getMinimumTicketPrices(List.of(event))).thenReturn(Map.of(4L, 50000L));
        when(storageService.generatePresignedUrl("events/4.png")).thenReturn("https://cdn.example.com/events/4.png");

        mockMvc.perform(get("/feeds/recommendeds").with(account.requestPostProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].minimumTicketPrice").value(50000));

        verify(feedService).getRecommendedEvents(42L);
    }

    @Test
    void search_shouldReturnCombinedSearchResults() throws Exception {
        when(searchService.search("music", 10)).thenReturn(List.of(
                new SearchService.SearchResult(1L, "EVENT", "Music Festival", "https://cdn.example.com/events/1.png", "Main Hall", null, null),
                new SearchService.SearchResult(2L, "ORGANIZATION", "Music Club", null, null, "https://cdn.example.com/orgs/2.png", true)
        ));

        mockMvc.perform(get("/feeds/search")
                        .param("q", "music")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("EVENT"))
                .andExpect(jsonPath("$[0].name").value("Music Festival"))
                .andExpect(jsonPath("$[0].bannerUrl").value("https://cdn.example.com/events/1.png"))
                .andExpect(jsonPath("$[0].venue").value("Main Hall"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("ORGANIZATION"))
                .andExpect(jsonPath("$[1].avatarUrl").value("https://cdn.example.com/orgs/2.png"))
                .andExpect(jsonPath("$[1].verified").value(true));

        verify(searchService).search("music", 10);
    }

    private Event createEvent(long id, String name) {
        return Event.builder()
                .id(id)
                .name(name)
                .organization(OrganizationAccount.builder()
                        .id(100L + id)
                        .name("Organization " + id)
                        .email("org" + id + "@example.com")
                        .passwordHash("hashed")
                        .build())
                .published(true)
                .isOnlineEvent(false)
                .venue("Venue " + id)
                .address("Address " + id)
                .dateTime(Instant.parse("2030-01-01T00:00:00Z"))
                .bannerKey("events/" + id + ".png")
                .build();
    }

}
