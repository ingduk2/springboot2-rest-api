package com.rest.api.controller.v1;

import com.rest.api.entity.User;
import com.rest.api.repo.UserJpaRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class SignControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepo userJpaRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void initData() {
        String uid = "ingduk2@naver.com";
        String name = "ingduk2";

        userJpaRepo.save(User.builder()
                .uid(uid)
                .password(passwordEncoder.encode("qwer1234"))
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
    }

    @Test
    void signin() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "ingduk2@naver.com");
        params.add("password", "qwer1234");
        mockMvc.perform(post("/v1/signin").params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());

    }

    @Test
    public void signinFail() throws Exception {
        MultiValueMap<String, String> paramgs = new LinkedMultiValueMap<>();
        paramgs.add("id", "ingduk2@naver.com");
        paramgs.add("password", "12345");
        mockMvc.perform(post("/v1/signin").params(paramgs))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-1001))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    public void signup() throws Exception {
        long epchTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "ingduk2_" + epchTime + "@naver.com");
        params.add("password", "12345");
        params.add("name", "ingduk2_" + epchTime);
        mockMvc.perform(post("/v1/signup").params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    public void signupFail() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "ingduk2@naver.com");
        params.add("password", "12345");
        params.add("name", "ingduk2");
        mockMvc.perform(post("/v1/signup").params(params))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-9999));

    }


    @Test
    @Disabled
    @WithMockUser(username = "mockUser", roles = {"USER"}) // 가상의 Mock 유저 대입
    public void accessdenied() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/exception/accessdenied"));
    }
}