package io.j13n.core.dao;

import io.j13n.core.commons.jooq.dao.AbstractUpdatableDAO;
import io.j13n.core.dto.search.Job;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import org.springframework.stereotype.Component;

import static io.j13n.core.jooq.core.Tables.CORE_JOBS;

@Component
public class JobDAO extends AbstractUpdatableDAO<CoreJobsRecord, Long, Job> {

    protected JobDAO() {
        super(Job.class, CORE_JOBS, CORE_JOBS.ID);
    }
}
