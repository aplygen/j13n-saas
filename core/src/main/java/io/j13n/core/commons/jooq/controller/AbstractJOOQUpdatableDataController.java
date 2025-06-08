package io.j13n.core.commons.jooq.controller;

import io.j13n.core.commons.base.model.dto.AbstractUpdatableDTO;
import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.jooq.dao.AbstractUpdatableDAO;
import io.j13n.core.commons.jooq.service.AbstractJOOQUpdatableDataService;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jooq.UpdatableRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class AbstractJOOQUpdatableDataController<
                R extends UpdatableRecord<R>,
                I extends Serializable,
                D extends AbstractUpdatableDTO<I, I>,
                O extends AbstractUpdatableDAO<R, I, D>,
                S extends AbstractJOOQUpdatableDataService<R, I, D, O>>
        extends AbstractJOOQDataController<R, I, D, O, S> {

    @PutMapping(AbstractJOOQDataController.PATH_ID)
    public CompletableFuture<ResponseEntity<D>> put(
            @PathVariable(name = PATH_VARIABLE_ID, required = false) final I id, @RequestBody D entity) {
        if (id != null) entity.setId(id);
        return VirtualThreadWrapper.map(this.service.update(entity), ResponseEntity::ok);
    }

    @PatchMapping(AbstractJOOQDataController.PATH_ID)
    public CompletableFuture<ResponseEntity<D>> patch(
            @PathVariable(name = PATH_VARIABLE_ID) final I id, @RequestBody Map<String, Object> entityMap) {
        return VirtualThreadWrapper.map(this.service.update(id, entityMap), ResponseEntity::ok);
    }
}
