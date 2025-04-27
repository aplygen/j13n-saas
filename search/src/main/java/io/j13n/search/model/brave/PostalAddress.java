package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PostalAddress implements Serializable {

    private String type;

    private String country;

    private String postalCode;

    private String streetAddress;

    private String addressRegion;

    private String addressLocality;

    private String displayAddress;
}
