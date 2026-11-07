package com.maltsev.conference_app.controller;

import java.security.Principal;

/** Имитация вошедшего пользователя в MockMvc-тестах: в продакшене Principal создаёт Spring Security из JWT. */
final class Auth {
    private Auth() {}

    static Principal user(String id) {
        return () -> id;
    }
}
