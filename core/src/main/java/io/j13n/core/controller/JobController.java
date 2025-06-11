package io.j13n.core.controller;

import io.j13n.core.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import io.j13n.core.dao.JobDAO;
import io.j13n.core.dto.search.Job;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import io.j13n.core.service.search.JobService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/jobs")
public class JobController extends AbstractJOOQUpdatableDataController<CoreJobsRecord, Long, Job, JobDAO, JobService> {
}
