package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class QAInfobox extends AbstractGraphInfobox {

    private String subtype;

    private QAPage data;

    private MetaUrl metaUrl;
}
