package io.j13n.core.service.search;

import io.j13n.core.commons.jooq.service.AbstractJOOQUpdatableDataService;
import io.j13n.core.dao.JobsDAO;
import io.j13n.core.dto.search.Jobs;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class JobsService extends AbstractJOOQUpdatableDataService<CoreJobsRecord, Long, Jobs, JobsDAO> {

    @Override
    protected CompletableFuture<Jobs> updatableEntity(Jobs entity) {
        return null;
    }
}
