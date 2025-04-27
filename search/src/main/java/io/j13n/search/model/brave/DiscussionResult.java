package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DiscussionResult extends SearchResult {

    private String type;

    private ForumData data;
}
