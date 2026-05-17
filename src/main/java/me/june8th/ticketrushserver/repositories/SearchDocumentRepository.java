package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchDocumentRepository extends ElasticsearchRepository<SearchDocument, String> {
}
