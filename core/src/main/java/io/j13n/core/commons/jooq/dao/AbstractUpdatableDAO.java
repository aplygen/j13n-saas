package io.j13n.core.commons.jooq.dao;

import io.j13n.core.commons.base.model.dto.AbstractUpdatableDTO;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Transactional
public abstract class AbstractUpdatableDAO<R extends UpdatableRecord<R>, I extends Serializable, D extends AbstractUpdatableDTO<I, I>>
        extends AbstractDAO<R, I, D> {

    private static final String UPDATED_BY = "UPDATED_BY";

    protected final Field<?> updatedByField;

    protected AbstractUpdatableDAO(Class<D> pojoClass, Table<R> table, Field<I> idField) {
        super(pojoClass, table, idField);
        this.updatedByField = table.field(UPDATED_BY);
    }
}
