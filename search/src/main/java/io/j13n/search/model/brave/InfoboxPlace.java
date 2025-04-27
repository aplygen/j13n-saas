package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InfoboxPlace extends AbstractGraphInfobox {

    private String subtype;

    private LocationResult location;
}
