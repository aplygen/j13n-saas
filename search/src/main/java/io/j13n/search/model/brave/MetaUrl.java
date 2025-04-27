package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MetaUrl implements Serializable {

    private String scheme;

    private String netloc;

    private String hostname;

    private String favicon;

    private String path;
}
