package com.example.eCampus.Auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoginRequestTest {

    @Test
    void loginRequestShouldContainUsernameAndPassword() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("student");
        request.setPassword("secret");

        assertEquals("student", request.getUsername());
        assertEquals("secret", request.getPassword());
    }

    @Test
    void registerRequestShouldContainUsernameAndPassword() {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("newstudent");
        request.setPassword("secret123");

        assertEquals("newstudent", request.getUsername());
        assertEquals("secret123", request.getPassword());
    }
}
