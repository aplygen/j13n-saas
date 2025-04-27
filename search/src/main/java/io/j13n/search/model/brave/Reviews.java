package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * The reviews associated with an entity.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Reviews implements Serializable {

    private List<TripAdvisorReview> results;

    private String viewMoreUrl;

    private Boolean reviewsInForeignLanguage;
}
