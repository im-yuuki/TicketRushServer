package me.june8th.ticketrushserver.contents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "ticketrush-search")
public class SearchDocument {

    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_ORGANIZATION = "ORGANIZATION";

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Long)
    private long sourceId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Boolean)
    private Boolean published;

    @Field(type = FieldType.Date)
    private Instant dateTime;

    @Field(type = FieldType.Boolean)
    private Boolean verified;

    public static String eventId(long eventId) {
        return TYPE_EVENT + ":" + eventId;
    }

    public static String organizationId(long organizationId) {
        return TYPE_ORGANIZATION + ":" + organizationId;
    }

}
