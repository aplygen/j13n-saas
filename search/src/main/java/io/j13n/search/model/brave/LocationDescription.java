package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * AI generated description of a location result.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocationDescription {

    /**
     * The type of a location description. The value is always local_description.
     */
    private String type;

    /**
     * A Temporary id of the location with this description.
     */
    private String id;

    /**
     * AI generated description of the location with the given id.
     */
    private String description;
}
