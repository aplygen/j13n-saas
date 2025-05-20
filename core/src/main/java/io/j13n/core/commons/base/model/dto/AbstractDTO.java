package io.j13n.core.commons.base.model.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public abstract class AbstractDTO<I extends Serializable, U extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 9168855669826067857L;

    @Id
    private I id;

    private LocalDateTime createdAt;
    private U createdBy;
}
