package io.j13n.core.enums;

import org.jooq.EnumType;

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
        return literal;
    }

    @Override
    public String getName() {
        return "core.file_resource_type";
    }
}
