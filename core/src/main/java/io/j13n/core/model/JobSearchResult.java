package io.j13n.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@FieldNameConstants
public class JobSearchResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 2892358632431906782L;

    private String title;
    private String company;
    private String location;
    private String description;
    private URI applicationUrl;
    private String source;
    private LocalDateTime postedDate;
    private boolean isRemote;
}
