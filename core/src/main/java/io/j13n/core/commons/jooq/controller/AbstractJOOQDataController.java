package io.j13n.core.commons.jooq.controller;

import io.j13n.core.commons.base.model.Query;
import io.j13n.core.commons.base.model.condition.AbstractCondition;
import io.j13n.core.commons.base.model.dto.AbstractDTO;
import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.base.util.ConditionUtil;
import io.j13n.core.commons.jooq.dao.AbstractDAO;
import io.j13n.core.commons.jooq.service.AbstractJOOQDataService;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import org.jooq.UpdatableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class AbstractJOOQDataController<
        R extends UpdatableRecord<R>,
        I extends Serializable,
        D extends AbstractDTO<I, I>,
        O extends AbstractDAO<R, I, D>,
        S extends AbstractJOOQDataService<R, I, D, O>> {

    public static final String PATH_VARIABLE_ID = "id";
    public static final String PATH_ID = "/{" + PATH_VARIABLE_ID + "}";
    public static final String PATH_QUERY = "query";

    protected S service;

    @Autowired
    private void setService(S service) {
        this.service = service;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<D>> create(@RequestBody D entity) {
        return VirtualThreadWrapper.map(this.service.create(entity), ResponseEntity::ok);
    }

    @GetMapping(PATH_ID)
    public CompletableFuture<ResponseEntity<D>> read(@PathVariable(PATH_VARIABLE_ID) final I id) {
        return VirtualThreadWrapper.fromCallable(() -> {
            D result = this.service.read(id).join();
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        });
    }

    @GetMapping()
    public CompletableFuture<ResponseEntity<Page<D>>> readPageFilter(
            Pageable pageable, @RequestParam MultiValueMap<String, String> params) {
        pageable = (pageable == null ? PageRequest.of(0, 10, Direction.ASC, PATH_VARIABLE_ID) : pageable);
        AbstractCondition condition = ConditionUtil.parameterMapToMap(params);
        return VirtualThreadWrapper.map(this.service.readPageFilter(pageable, condition), ResponseEntity::ok);
    }

    @PostMapping(PATH_QUERY)
    public CompletableFuture<ResponseEntity<Page<D>>> readPageFilter(@RequestBody Query query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), query.getSort());
        return VirtualThreadWrapper.map(
                this.service.readPageFilter(pageable, query.getCondition()), ResponseEntity::ok);
    }

    @DeleteMapping(PATH_ID)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public CompletableFuture<Integer> delete(@PathVariable(PATH_VARIABLE_ID) final I id) {
        return this.service.delete(id);
    }
}
