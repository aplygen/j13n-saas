package io.j13n.core.controller;

import io.j13n.core.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import io.j13n.core.dao.JobsDAO;
import io.j13n.core.dto.search.Jobs;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import io.j13n.core.service.search.JobsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/jobs")
public class JobsController extends AbstractJOOQUpdatableDataController<CoreJobsRecord, Long, Jobs, JobsDAO, JobsService> {
}
