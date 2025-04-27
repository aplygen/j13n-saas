package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The ranking order of results on a search result page.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MixedResponse {

    /**
     * The type representing the model mixed. The value is always mixed.
     */
    private String type;

    /**
     * The ranking order for the main section of the search result page.
     */
    private List<ResultReference> main;

    /**
     * The ranking order for the top section of the search result page.
     */
    private List<ResultReference> top;

    /**
     * The ranking order for the side section of the search result page.
     */
    private List<ResultReference> side;
}
