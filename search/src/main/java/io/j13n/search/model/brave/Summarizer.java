package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Details on getting the summary.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Summarizer {

    /**
     * The value is always summarizer.
     */
    private String type;

    /**
     * The key for the summarizer API.
     */
    private String key;
}
