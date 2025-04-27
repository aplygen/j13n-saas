package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * A model representing a unit of measurement.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Unit implements Serializable {

    private Float value;

    private String units;
}
