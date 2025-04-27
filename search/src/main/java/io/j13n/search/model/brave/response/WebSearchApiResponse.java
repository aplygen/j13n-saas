package io.j13n.search.model.brave.response;

import io.j13n.search.model.brave.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Top level response model for successful Web Search API requests.
 * The response will include the relevant keys based on the plan subscribed,
 * query relevance or applied result_filter as a query parameter.
 * The API can also respond back with an error response based on invalid
 * subscription keys and rate limit events.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WebSearchApiResponse implements Serializable {

    /**
     * The type of web search API result. The value is always search.
     */
    private String type;

    /**
     * Discussions clusters aggregated from forum posts that are relevant to the query.
     */
    private Discussions discussions;

    /**
     * Frequently asked questions that are relevant to the search query.
     */
    private FAQ faq;

    /**
     * Aggregated information on an entity showable as an infobox.
     */
    private GraphInfobox infobox;

    /**
     * Places of interest (POIs) relevant to location sensitive queries.
     */
    private Locations locations;

    /**
     * Preferred ranked order of search results.
     */
    private MixedResponse mixed;

    /**
     * News results relevant to the query.
     */
    private News news;

    /**
     * Search query string and its modifications that are used for search.
     */
    private Query query;

    /**
     * Videos relevant to the query.
     */
    private Videos videos;

    /**
     * Web search results relevant to the query.
     */
    private Search web;

    /**
     * Summary key to get summary results for the query.
     */
    private Summarizer summarizer;

    /**
     * Callback information for rich results.
     */
    private RichCallbackInfo rich;
}
