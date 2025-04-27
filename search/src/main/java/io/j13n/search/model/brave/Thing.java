package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * A model describing a generic thing.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Thing implements Serializable {

    private String type;

    private String name;

    private String url;

    private Thumbnail thumbnail;
}
