package io.github.lmqvq.lldcard.backend.config;

import io.github.lmqvq.lldcard.backend.entity.Admin;
import io.github.lmqvq.lldcard.backend.mapper.AdminMapper;
import io.github.lmqvq.lldcard.backend.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MINIMUM_PASSWORD_LENGTH = 12;

    private final AdminMapper adminMapper;
    private final String username;
    private final String password;

    public AdminBootstrap(
            AdminMapper adminMapper,
            @Value("${lldcard.bootstrap.admin.username:}") String username,
            @Value("${lldcard.bootstrap.admin.password:}") String password) {
        this.adminMapper = adminMapper;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isEmpty() && password.isEmpty()) {
            logger.info("Administrator bootstrap is disabled");
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalStateException("Bootstrap administrator username must contain 3 to 50 characters");
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalStateException("Bootstrap administrator password must contain at least 12 characters");
        }

        if (adminMapper.findByUsername(username) != null) {
            logger.info("Bootstrap administrator already exists");
            return;
        }

        Admin admin = new Admin(username, PasswordUtil.hashPassword(password));
        adminMapper.insertAdmin(admin);
        logger.info("Bootstrap administrator created");
    }
}