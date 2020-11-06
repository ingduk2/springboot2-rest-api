package com.rest.api.repo;

import com.rest.api.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserJpaRepoTest {
    @Autowired
    private UserJpaRepo userJpaRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void whenFindByUid_thenReturnUser() {
        String uid = "ingduk2@naver.com";
        String name = "ingduk2";

        //given
        userJpaRepo.save(User.builder()
                        .uid(uid)
                        .password(passwordEncoder.encode("qwer1234"))
                        .name(name)
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build());

        //when
        Optional<User> user = userJpaRepo.findByUid(uid);

        //then
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user).isPresent();
        Assertions.assertThat(user.get().getName()).isEqualTo(name);
    }

    @Test
    public void selectTest() {
        //when
        Optional<User> user = userJpaRepo.findByUid("ingduk2@naver.com");

        //then
        Assertions.assertThat(user).isNotNull();
    }
}