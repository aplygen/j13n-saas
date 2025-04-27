package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The ranking order of results on a search result page.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ResultReference {

    /**
     * The type of the result.
     */
    private String type;

    /**
     * The 0th based index where the result should be placed.
     */
    private Integer index;

    /**
     * Whether to put all the results from the type at specific position.
     */
    private Boolean all;
}
