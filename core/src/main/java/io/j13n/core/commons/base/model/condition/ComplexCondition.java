package io.j13n.core.commons.base.model.condition;

import io.j13n.core.commons.base.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ComplexCondition extends AbstractCondition {

    @Serial
    private static final long serialVersionUID = 9191279295177801553L;

    private ComplexConditionOperator operator;
    private List<AbstractCondition> conditions;

    public static ComplexCondition and(AbstractCondition... conditions) {
        return new ComplexCondition().setConditions(List.of(conditions)).setOperator(ComplexConditionOperator.AND);
    }

    public static ComplexCondition or(AbstractCondition... conditions) {
        return new ComplexCondition().setConditions(List.of(conditions)).setOperator(ComplexConditionOperator.OR);
    }

    @Override
    public List<FilterCondition> findConditionWithField(String fieldName) {
        if (StringUtil.safeIsBlank(fieldName)) {
            return List.of();
        }

        return this.conditions.stream()
                .flatMap(condition -> condition.findConditionWithField(fieldName).stream())
                .toList();
    }

    @Override
    public boolean isEmpty() {

        return conditions.isEmpty();
    }
}
