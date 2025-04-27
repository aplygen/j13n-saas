package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A model representing a video result.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class VideoResult extends Result {

    /**
     * The type identifying the video result. The value is always video_result.
     */
    private String type;

    /**
     * Meta data for the video.
     */
    private VideoData video;

    /**
     * Aggregated information on the URL
     */
    private MetaUrl metaUrl;

    /**
     * The thumbnail of the video.
     */
    private Thumbnail thumbnail;

    /**
     * A string representing the age of the video.
     */
    private String age;
}
