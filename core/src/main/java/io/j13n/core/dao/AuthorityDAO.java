package io.j13n.core.dao;

import io.j13n.core.commons.jooq.dao.AbstractUpdatableDAO;
import io.j13n.core.dto.user.Authority;
import io.j13n.core.jooq.tables.records.CoreAuthoritiesRecord;
import org.springframework.stereotype.Component;

import static io.j13n.core.jooq.tables.CoreAuthorities.CORE_AUTHORITIES;

@Component
public class AuthorityDAO extends AbstractUpdatableDAO<CoreAuthoritiesRecord, Long, Authority> {

    protected AuthorityDAO() {
        super(Authority.class, CORE_AUTHORITIES, CORE_AUTHORITIES.ID);
    }
}
