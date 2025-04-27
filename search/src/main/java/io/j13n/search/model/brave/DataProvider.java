package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataProvider implements Serializable {

    private String type;

    private String name;

    private String url;

    private String longName;

    private String img;
}
