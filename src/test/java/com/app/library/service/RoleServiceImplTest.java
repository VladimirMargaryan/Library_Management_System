package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Receipt;
import com.app.library.model.ReceiptStatus;
import com.app.library.model.Role;
import com.app.library.repository.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;
    private RoleServiceImpl underTestRoleService;

    @BeforeEach
    void setUp() {
        underTestRoleService = new RoleServiceImpl(roleRepository);
    }

    @AfterEach
    void tearDown() {
        roleRepository.deleteAll();
    }

    @Test
    void CanGetByID() throws NotFoundException {
        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        doReturn(Optional.of(givenRole)).when(roleRepository).findById(1L);

        //when
        Role result = underTestRoleService.getByID(1L);

        //then
        verify(roleRepository).findById(1L);
        assertThat(givenRole).isEqualTo(result);

    }

    @Test
    void CanNotGetByID() {
        //given
        long roleId = 1L;

        //then
        assertThatThrownBy(() -> underTestRoleService.getByID(roleId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role not found!");
    }


    @Test
    void CanGetByName() {

        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        // when
        given(roleRepository.findByName(givenRole.getName()))
                .willReturn(givenRole);

        // then
        assertThat(underTestRoleService.getByName(givenRole.getName()))
                .isEqualTo(givenRole);
    }

    @Test
    void CanNotGetByNameInCaseOfNull() {

        // then
        assertThat(underTestRoleService.getByName(anyString()))
                .isEqualTo(null);

    }


    @Test
    void save() {
        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        //when
        underTestRoleService.save(givenRole);

        //then
        ArgumentCaptor<Role> roleArgumentCaptor =
                ArgumentCaptor.forClass(Role.class);

        verify(roleRepository)
                .save(roleArgumentCaptor.capture());

        Role capturedRole = roleArgumentCaptor.getValue();

        assertThat(capturedRole).isEqualTo(givenRole);
    }

    @Test
    void getAll() {
        // when
        underTestRoleService.getAll();

        // then
        verify(roleRepository).findAll();
    }

    @Test
    void CanRemoveById() throws NotFoundException {
        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        doReturn(Optional.of(givenRole)).when(roleRepository).findById(givenRole.getId());

        //when
        underTestRoleService.removeById(givenRole.getId());

        //then
        verify(roleRepository).deleteById(givenRole.getId());
    }

    @Test
    void CanNotRemoveById() throws NotFoundException {
        // given
        long givenRoleId = 1L;

        //then
        assertThatThrownBy(() -> underTestRoleService.removeById(givenRoleId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role not found by the id " + givenRoleId);
    }


    @Test
    void CanUpdate() throws NotFoundException {
        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        when(roleRepository.findById(givenRole.getId()))
                .thenReturn(Optional.of(givenRole));

        ArgumentCaptor<Role> argumentCaptor =
                ArgumentCaptor.forClass(Role.class);

        when(roleRepository.save(argumentCaptor.capture()))
                .thenAnswer(iom -> iom.getArgument(0));


        // When
        givenRole.setName("FAKE_NAME");
        Role updated = underTestRoleService.update(givenRole);


        // Then
        assertThat(argumentCaptor.getValue().getName())
                .isEqualTo(updated.getName());
    }

    @Test
    void CanNotUpdate() {
        // given
        Role givenRole = new Role(1L, "TEST_ROLE");

        //then
        assertThatThrownBy(() -> underTestRoleService.update(givenRole))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role not found");

    }

}