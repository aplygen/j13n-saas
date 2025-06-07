package io.j13n.core.controller;

import io.j13n.core.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import io.j13n.core.dao.UserDAO;
import io.j13n.core.dto.user.User;
import io.j13n.core.jooq.core.tables.records.CoreUsersRecord;
import io.j13n.core.service.user.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/security/users")
public class UserController
        extends AbstractJOOQUpdatableDataController<CoreUsersRecord, Long, User, UserDAO, UserService> {}
