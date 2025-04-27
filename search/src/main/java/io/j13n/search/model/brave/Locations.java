package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A model representing location results.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Locations {

    /**
     * Location type identifier. The value is always locations.
     */
    private String type;

    /**
     * An aggregated list of location sensitive results.
     */
    private List<LocationResult> results;
}
