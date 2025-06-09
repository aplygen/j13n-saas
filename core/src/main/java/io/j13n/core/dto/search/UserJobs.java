package io.j13n.core.dto.search;

import io.j13n.core.commons.base.model.dto.AbstractUpdatableDTO;
import io.j13n.core.enums.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString(callSuper = true)
public class UserJobs extends AbstractUpdatableDTO<Long, Long> {

    @Serial
    private static final long serialVersionUID = 5053833118220755405L;

    private Long userId;
    private Long jobId;
    private JobStatus status = JobStatus.IN_PROGRESS;

    private JobStatus jobStatus;
    private LocalDateTime appliedDate;
    private String applicationNotes;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterPhone;
    private String interviewNotes;
    private LocalDateTime nextInterviewDate;
    private String salaryRange;
    private String benefits;
    private String rejectionReason;
    private LocalDateTime rejectionDate;
    private String offerDetails;
    private LocalDateTime offerDate;
    private String negotiationNotes;
}
