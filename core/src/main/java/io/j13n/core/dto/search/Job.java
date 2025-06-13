package io.j13n.core.dto.search;

import io.j13n.core.commons.base.model.dto.AbstractUpdatableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString(callSuper = true)
public class Job extends AbstractUpdatableDTO<Long, Long> {

    private static final String DEFAULT = "UNKNOWN";

    @Serial
    private static final long serialVersionUID = 2478806976919905198L;

    private String title;
    private String company = DEFAULT;
    private String location;
    private String description;
    private String applicationUrl;
    private String source = DEFAULT;
    private LocalDateTime postedDate;
    private boolean isRemote = Boolean.FALSE;
    private boolean isExpired = Boolean.FALSE;
}
