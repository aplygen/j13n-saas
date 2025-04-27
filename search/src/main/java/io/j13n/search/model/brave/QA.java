package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class QA implements Serializable {

    private String question;

    private String answer;

    private String title;

    private String url;

    private MetaUrl metaUrl;
}
