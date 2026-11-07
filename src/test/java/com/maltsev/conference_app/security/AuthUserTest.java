package com.maltsev.conference_app.security;

import com.maltsev.conference_app.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthUserTest {
    @Test
    void id_returnsNumericPrincipalName() {
        assertEquals(42L, AuthUser.id(() -> "42"));
    }

    @Test
    void id_nullPrincipal_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> AuthUser.id(null));
    }

    @Test
    void id_nonNumericPrincipal_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> AuthUser.id(() -> "not-a-number"));
    }
}
