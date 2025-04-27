package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InfoboxWithLocation extends AbstractGraphInfobox {

    private String subtype;

    private Boolean isLocation;

    private List<Float> coordinates;

    private Integer zoomLevel;

    private LocationResult location;
}
