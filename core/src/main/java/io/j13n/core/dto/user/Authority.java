package io.j13n.core.dto.user;

import io.j13n.commons.model.dto.AbstractUpdatableDTO;
import io.j13n.commons.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString(callSuper = true)
public class Authority extends AbstractUpdatableDTO<Long, Long> {

    private String name;
    private String description;

    public Authority setName(String name) {
        this.name = StringUtil.toUpperCaseWithUnderscores(name);
        return this;
    }
}
