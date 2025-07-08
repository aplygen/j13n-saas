package io.j13n.core.enums;

import io.j13n.core.jooq.core.Core;
import lombok.Getter;
import org.jooq.EnumType;
import org.jooq.Schema;

@Getter
public enum JobStatus implements EnumType {
    IN_SEARCH("IN_SEARCH"),

    DISCARD("DISCARD"),

    SAVED("SAVED"),

    IN_PROGRESS("IN_PROGRESS"),

    APPLIED("APPLIED"),

    INTERVIEWING("INTERVIEWING"),

    NEGOTIATION("NEGOTIATION");

    private final String literal;

    JobStatus(String literal) {
        this.literal = literal;
    }

    public static JobStatus lookupLiteral(String literal) {
        return EnumType.lookupLiteral(JobStatus.class, literal);
    }

    @Override
    public String getLiteral() {
        return this.literal;
    }

    @Override
    public Schema getSchema() {
        return Core.CORE;
    }

    @Override
    public String getName() {
        return "core.job_result_status";
    }
}
