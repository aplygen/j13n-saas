package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The hint for the rich result.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RichCallbackHint {

    /**
     * The name of the vertical of the rich result.
     */
    private String vertical;

    /**
     * The callback key for the rich result.
     */
    private String callback_key;
}
