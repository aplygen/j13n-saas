package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A model representing video results.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Videos {

    /**
     * The type representing the videos. The value is always videos.
     */
    private String type;

    /**
     * A list of video results.
     */
    private List<VideoResult> results;

    /**
     * Whether the video results are changed by a Goggle. False by default.
     */
    private Boolean mutatedByGoggles;
}
