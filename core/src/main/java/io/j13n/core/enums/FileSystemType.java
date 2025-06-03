package io.j13n.core.enums;

import org.jooq.EnumType;

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
    public String getName() {
        return name();
    }
}
