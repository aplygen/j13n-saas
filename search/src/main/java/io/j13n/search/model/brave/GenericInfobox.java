package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GenericInfobox extends AbstractGraphInfobox {

    private String subtype;

    private List<String> foundInUrls;
}
