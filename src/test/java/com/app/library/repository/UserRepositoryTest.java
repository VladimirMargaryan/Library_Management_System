package com.app.library.repository;

import com.app.library.model.Gender;
import com.app.library.model.User;
import com.app.library.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void search() {

        // given

        String givenKeyword = Gender.MALE.toString();

        User user1 = new User(
                "john",
                "watson",
                "evrwbregew@email",
                "wqenclkwncmlwmac",
                "32905730",
                Gender.MALE
        );
        user1.setStatus(UserStatus.VERIFIED);

        User user2 = new User(
                "Jack",
                "Barret",
                "efefeleev@mail",
                "fwefvhwevonewncqlj",
                "40539271034",
                Gender.MALE
        );

        user2.setStatus(UserStatus.VERIFIED);

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("id"));

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        Page<User> userPage = new PageImpl<>(users, pageRequest, 5);

        underTest.save(user1);
        underTest.save(user2);


        // when
        Page<User> result = underTest.search(givenKeyword, pageRequest);

        // then
        assertThat(result.getContent()).isEqualTo(userPage.getContent());

    }
}