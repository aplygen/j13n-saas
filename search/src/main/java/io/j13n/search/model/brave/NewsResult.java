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
public class NewsResult extends Result {

    /**
     * The aggregated information on the url representing a news result
     */
    private MetaUrl metaUrl;

    /**
     * The source of the news.
     */
    private String source;

    /**
     * Whether the news result is currently a breaking news.
     */
    private Boolean breaking;

    /**
     * Whether the news result is currently live.
     */
    private Boolean isLive;

    /**
     * The thumbnail associated with the news result.
     */
    private Thumbnail thumbnail;

    /**
     * A string representing the age of the news article.
     */
    private String age;

    /**
     * A list of extra alternate snippets for the news search result.
     */
    private List<String> extraSnippets;
}
