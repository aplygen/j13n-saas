package io.j13n.core.enums;

import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;

public enum UserStatusCode implements EnumType {
    ACTIVE("ACTIVE"),

    INACTIVE("INACTIVE"),

    DELETED("DELETED"),

    LOCKED("LOCKED"),

    PASSWORD_EXPIRED("PASSWORD_EXPIRED");

    private final String literal;

    UserStatusCode(String literal) {
        this.literal = literal;
    }

    public static UserStatusCode lookupLiteral(String literal) {
        return EnumType.lookupLiteral(UserStatusCode.class, literal);
    }

    @Override
    public Catalog getCatalog() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    public Boolean isActive() {
        return this == ACTIVE;
    }

    public Boolean isInActive() {
        return this == INACTIVE || this == DELETED || this == LOCKED || this == PASSWORD_EXPIRED;
    }
}
