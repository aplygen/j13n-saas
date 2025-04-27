package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A model representing metadata gathered for a video.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class VideoData {

    /**
     * A time string representing the duration of the video. The format can be HH:MM:SS or MM:SS.
     */
    private String duration;

    /**
     * The number of views of the video.
     */
    private String views;

    /**
     * The creator of the video.
     */
    private String creator;

    /**
     * The publisher of the video.
     */
    private String publisher;

    /**
     * A thumbnail associated with the video.
     */
    private Thumbnail thumbnail;

    /**
     * A list of tags associated with the video.
     */
    private List<String> tags;

    /**
     * Author of the video.
     */
    private Profile author;

    /**
     * Whether the video requires a subscription to watch.
     */
    private Boolean requiresSubscription;
}
