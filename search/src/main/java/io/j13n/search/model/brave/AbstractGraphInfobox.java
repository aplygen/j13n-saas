package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class AbstractGraphInfobox extends Result {

    private String type;

    private Integer position;

    private String label;

    private String category;

    private String longDesc;

    private Thumbnail thumbnail;

    private List<List<String>> attributes;

    private List<?> profiles;

    private String websiteUrl;

    private List<Rating> ratings;

    private List<DataProvider> providers;

    private Unit distance;

    private List<Thumbnail> images;

    private MovieData movie;
}
