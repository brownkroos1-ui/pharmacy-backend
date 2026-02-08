package com.pharmacy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    

        @Test

        public void testRegisterAndLoginEndpoints() throws Exception {

            String username = "testuser_" + System.currentTimeMillis();
            String name = "Test User";
            String email = username + "@example.com";

            String password = "password";

    

            // Test registration

            mockMvc.perform(post("/api/auth/register")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content("{\"username\":\"" + username + "\", \"name\":\"" + name + "\", \"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))

                    .andExpect(status().isCreated());

    

            // Test login

            mockMvc.perform(post("/api/auth/login")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}"))

                    .andExpect(status().isOk());

        }

    }

    
