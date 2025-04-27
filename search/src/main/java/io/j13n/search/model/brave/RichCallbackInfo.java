package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Callback information for rich results.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RichCallbackInfo {

    /**
     * The value is always rich.
     */
    private String type;

    /**
     * The hint for the rich result.
     */
    private RichCallbackHint hint;
}
