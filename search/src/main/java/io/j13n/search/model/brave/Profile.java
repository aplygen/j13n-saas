package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Profile implements Serializable {

    private String name;

    private String longName;

    private String url;

    private String img;
}
