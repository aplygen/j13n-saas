package io.j13n.search.dto;

import io.j13n.commons.dto.ObjectWithUniqueID;
import io.j13n.search.model.brave.WebSearchApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SearchResultDTO extends ObjectWithUniqueID<WebSearchApiResponse> {

    private String query;

    private String provider;

    private LocalDateTime timestamp;

    private String userId;

    private String ipAddress;

    private String userAgent;

    public SearchResultDTO(WebSearchApiResponse response) {
        super(response);
        this.timestamp = LocalDateTime.now();
    }

    public SearchResultDTO(WebSearchApiResponse response, String query, String provider) {
        super(response);
        this.query = query;
        this.provider = provider;
        this.timestamp = LocalDateTime.now();
    }
}
