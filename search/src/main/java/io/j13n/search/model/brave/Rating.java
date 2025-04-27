package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * The rating associated with an entity.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Rating implements Serializable {

    private Float ratingValue;

    private Float bestRating;

    private Integer reviewCount;

    private Profile profile;

    private Boolean isTripadvisor;
}
