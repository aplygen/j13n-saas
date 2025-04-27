package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A model representing news results.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class News {

    /**
     * The type representing the news. The value is always news.
     */
    private String type;

    /**
     * A list of news results.
     */
    private List<NewsResult> results;

    /**
     * Whether the news results are changed by a Goggle. False by default.
     */
    private Boolean mutatedByGoggles;
}
