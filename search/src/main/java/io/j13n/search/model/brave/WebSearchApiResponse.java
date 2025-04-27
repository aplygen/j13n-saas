package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @deprecated Use {@link io.j13n.search.model.brave.response.WebSearchApiResponse} instead.
 */
@Deprecated
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WebSearchApiResponse implements Serializable {

    private String type;

    private Discussions discussions;

    private FAQ faq;

    private GraphInfobox infobox;

    private Locations locations;

    private MixedResponse mixed;

    private News news;

    private Query query;

    private Videos videos;

    private Search web;

    private Summarizer summarizer;

    private RichCallbackInfo rich;
}
