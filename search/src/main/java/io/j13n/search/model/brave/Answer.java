package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Answer implements Serializable {

    private String text;

    private String author;

    private Integer upvoteCount;

    private Integer downvoteCount;
}
