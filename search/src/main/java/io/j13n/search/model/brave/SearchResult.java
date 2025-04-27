package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SearchResult extends Result {

    private String type;

    private String subtype;

    private Boolean isLive;

    private DeepResult deepResults;

    private List<List<Object>> schemas;

    private MetaUrl metaUrl;

    private Thumbnail thumbnail;

    private String age;

    private String language;

    private LocationResult location;

    private VideoData video;

    private MovieData movie;

    private FAQ faq;

    private QAPage qa;

    private Book book;

    private Rating rating;

    private Article article;

    private ProductReview product;

    private List<ProductReview> productCluster;

    private String clusterType;

    private List<Result> cluster;

    private CreativeWork creativeWork;

    private MusicRecording musicRecording;

    private Review review;

    private Software software;

    private Recipe recipe;

    private Organization organization;

    private String contentType;

    private List<String> extraSnippets;
}
