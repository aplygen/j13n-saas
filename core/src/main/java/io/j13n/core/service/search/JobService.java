package io.j13n.core.service.search;

import io.j13n.core.commons.jooq.service.AbstractJOOQUpdatableDataService;
import io.j13n.core.dao.JobDAO;
import io.j13n.core.dto.search.Job;
import io.j13n.core.jooq.core.tables.records.CoreJobsRecord;
import io.j13n.core.model.JobSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class JobService extends AbstractJOOQUpdatableDataService<CoreJobsRecord, Long, Job, JobDAO> {

    @Override
    protected CompletableFuture<Job> updatableEntity(Job entity) {
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

    public CompletableFuture<List<Job>> saveJobResult(List<JobSearchResult> jobSearchResults) {
        List<CompletableFuture<Job>> futures = jobSearchResults.stream().map()
    }

    public CompletableFuture<List<Job>> saveJobs(List<Job> jobs) {
        List<CompletableFuture<Job>> futures = jobs.stream()
                .map(this::create)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }
}
