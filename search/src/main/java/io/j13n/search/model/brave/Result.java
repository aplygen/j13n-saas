package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Result implements Serializable {

    private String title;

    private String url;

    private Boolean isSourceLocal;

    private Boolean isSourceBoth;

    private String description;

    private String pageAge;

    private String pageFetched;

    private Profile profile;

    private String language;

    private Boolean familyFriendly;
}
