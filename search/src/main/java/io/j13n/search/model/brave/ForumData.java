package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ForumData implements Serializable {

    private String forumName;

    private Integer numAnswers;

    private String score;

    private String title;

    private String question;

    private String topComment;
}
