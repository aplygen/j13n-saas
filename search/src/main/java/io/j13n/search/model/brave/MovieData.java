package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MovieData implements Serializable {

    private String name;

    private String description;

    private String url;

    private Thumbnail thumbnail;

    private String release;

    private List<Person> directors;

    private List<Person> actors;

    private Rating rating;

    private String duration;

    private List<String> genre;

    private String query;
}
