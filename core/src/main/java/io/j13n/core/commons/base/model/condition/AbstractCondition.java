package io.j13n.core.commons.base.model.condition;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Component
public abstract class AbstractCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 5748516741365718190L;

    private boolean negate = false;

    public abstract List<FilterCondition> findConditionWithField(String fieldName);

    public abstract boolean isEmpty();
}
