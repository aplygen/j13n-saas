package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * A model representing a Tripadvisor review.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TripAdvisorReview implements Serializable {

    private String title;

    private String description;

    private String date;

    private Rating rating;

    private Person author;

    private String reviewUrl;

    private String language;
}
