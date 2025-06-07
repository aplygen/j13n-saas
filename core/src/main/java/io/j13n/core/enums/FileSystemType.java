package io.j13n.core.enums;

import io.j13n.core.jooq.core.Core;
import org.jooq.EnumType;
import org.jooq.Schema;

public enum FileSystemType implements EnumType {
    FILE("FILE"),

    DIRECTORY("DIRECTORY");

    private final String literal;

    FileSystemType(String literal) {
        this.literal = literal;
    }

    public static FileSystemType lookupLiteral(String literal) {
        return EnumType.lookupLiteral(FileSystemType.class, literal);
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public Schema getSchema() {
        return Core.CORE;
    }

    @Override
    public String getName() {
        return "core.file_system_type";
    }
}
