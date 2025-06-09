package io.j13n.core.service.search;

import io.j13n.core.commons.jooq.service.AbstractJOOQUpdatableDataService;
import io.j13n.core.dao.JobsDAO;
import io.j13n.core.dto.search.Jobs;
import io.j13n.core.enums.JobStatus;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class JobsService extends AbstractJOOQUpdatableDataService<CoreJobsRecord, Long, Jobs, JobsDAO> {

    @Override
    protected CompletableFuture<Jobs> updatableEntity(Jobs entity) {
        return CompletableFuture.supplyAsync(() -> {
            if (entity.getId() != null) {
                return super.read(entity.getId())
                        .thenApply(existing -> {
                            existing.setTitle(entity.getTitle());
                            existing.setCompany(entity.getCompany());
                            existing.setLocation(entity.getLocation());
                            existing.setDescription(entity.getDescription());
                            existing.setApplicationUrl(entity.getApplicationUrl());
                            existing.setSource(entity.getSource());
                            existing.setPostedDate(entity.getPostedDate());
                            existing.setRemote(entity.isRemote());
                            return existing;
                        })
                        .join();
            }
            return entity;
        });
    }

    /**
     * Saves multiple jobs in batch
     *
     * @param jobs the list of jobs to save
     * @return a CompletableFuture containing the list of saved jobs
     */
    public CompletableFuture<List<Jobs>> saveJobs(List<Jobs> jobs) {
        List<CompletableFuture<Jobs>> futures = jobs.stream()
                .map(this::create)
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
