package io.j13n.core.enums;

import io.j13n.core.jooq.core.Core;
import org.jooq.EnumType;
import org.jooq.Schema;

public enum FileResourceType implements EnumType {
    STATIC("STATIC"),
    SECURED("SECURED");

    private final String literal;

    FileResourceType(String literal) {
        this.literal = literal;
    }

    public static FileResourceType lookupLiteral(String literal) {
        return EnumType.lookupLiteral(FileResourceType.class, literal);
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
        return "core.file_resource_type";
    }
}
