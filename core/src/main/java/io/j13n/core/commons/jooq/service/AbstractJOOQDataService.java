package io.j13n.core.commons.jooq.service;

import io.j13n.commons.model.condition.AbstractCondition;
import io.j13n.commons.model.dto.AbstractDTO;
import io.j13n.commons.thread.VirtualThreadWrapper;
import io.j13n.core.commons.jooq.dao.AbstractDAO;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class AbstractJOOQDataService<
        R extends UpdatableRecord<R>,
        I extends Serializable,
        D extends AbstractDTO<I, I>,
        O extends AbstractDAO<R, I, D>> {

    protected final Logger logger;

    protected O dao;

    protected AbstractJOOQDataService() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Autowired
    private void setDao(O dao) {
        this.dao = dao;
    }

    public CompletableFuture<D> create(D entity) {
        return VirtualThreadWrapper.fromCallable(() -> {
            entity.setCreatedBy(null);
            return getLoggedInUserId()
                    .thenApply(userId -> {
                        if (userId != null) {
                            entity.setCreatedBy(userId);
                        }
                        return entity;
                    })
                    .thenCompose(e -> this.dao.create(e))
                    .join();
        });
    }

    protected CompletableFuture<I> getLoggedInUserId() {
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<D> read(I id) {
        return this.dao.readById(id);
    }

    public CompletableFuture<Page<D>> readPageFilter(Pageable pageable, AbstractCondition condition) {
        return this.dao.readPageFilter(pageable, condition);
    }

    public CompletableFuture<List<D>> readAllFilter(AbstractCondition condition) {
        return this.dao.readAll(condition);
    }

    public CompletableFuture<Integer> delete(I id) {
        return this.dao.delete(id);
    }
}
