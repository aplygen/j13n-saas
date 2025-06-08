package io.j13n.core.dao;

import io.j13n.core.commons.jooq.dao.AbstractUpdatableDAO;
import io.j13n.core.dto.search.Jobs;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import org.springframework.stereotype.Component;

import static io.j13n.core.jooq.core.Tables.CORE_JOBS;

@Component
public class JobsDAO extends AbstractUpdatableDAO<CoreJobsRecord, Long, Jobs> {

    protected JobsDAO() {
        super(Jobs.class, CORE_JOBS, CORE_JOBS.ID);
    }
}
