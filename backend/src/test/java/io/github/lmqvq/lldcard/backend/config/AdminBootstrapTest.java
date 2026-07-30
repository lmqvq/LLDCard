package io.github.lmqvq.lldcard.backend.config;

import io.github.lmqvq.lldcard.backend.entity.Admin;
import io.github.lmqvq.lldcard.backend.mapper.AdminMapper;
import io.github.lmqvq.lldcard.backend.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private AdminMapper adminMapper;

    @Test
    void skipsBootstrapWhenCredentialsAreEmpty() {
        AdminBootstrap bootstrap = new AdminBootstrap(adminMapper, "", "");

        bootstrap.run(null);

        verifyNoInteractions(adminMapper);
    }

    @Test
    void rejectsWeakBootstrapPassword() {
        AdminBootstrap bootstrap = new AdminBootstrap(adminMapper, "admin", "short");

        assertThrows(IllegalStateException.class, () -> bootstrap.run(null));
    }

    @Test
    void doesNotOverwriteExistingAdministrator() {
        when(adminMapper.findByUsername("admin")).thenReturn(new Admin("admin", "existing"));
        AdminBootstrap bootstrap = new AdminBootstrap(adminMapper, "admin", "StrongPassword123!");

        bootstrap.run(null);

        verify(adminMapper, never()).insertAdmin(org.mockito.ArgumentMatchers.any(Admin.class));
    }

    @Test
    void createsAdministratorWithHashedPassword() {
        when(adminMapper.findByUsername("admin")).thenReturn(null);
        AdminBootstrap bootstrap = new AdminBootstrap(adminMapper, "admin", "StrongPassword123!");

        bootstrap.run(null);

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminMapper).insertAdmin(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertTrue(PasswordUtil.verifyPasswordSimple("StrongPassword123!", captor.getValue().getPassword()));
    }
}