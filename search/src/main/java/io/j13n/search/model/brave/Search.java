package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Search implements Serializable {

    private String type;

    private List<SearchResult> results;

    private Boolean familyFriendly;
}
