package edu.ucsb.cs156.organic.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import edu.ucsb.cs156.organic.entities.User;
import edu.ucsb.cs156.organic.repositories.UserEmailRepository;
import edu.ucsb.cs156.organic.repositories.UserRepository;
import edu.ucsb.cs156.organic.testconfig.TestConfig;

import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@WebMvcTest(controllers = UsersController.class)
@Import(TestConfig.class)
@AutoConfigureDataJpa
public class UsersControllerTests extends ControllerTestCase {

  @MockBean
  UserRepository userRepository;

  @MockBean
  UserEmailRepository userEmailRepository;

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void users__admin_logged_in() throws Exception {

    // arrange

    User u1 = User.builder().githubId(1).build();
    User u2 = User.builder().githubId(2).build();

    ArrayList<User> expectedUsers = new ArrayList<>();
    expectedUsers.addAll(Arrays.asList(u1, u2));

    when(userRepository.findAll()).thenReturn(expectedUsers);
    String expectedJson = mapper.writeValueAsString(expectedUsers);

    // act

    MvcResult response = mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isOk()).andReturn();

    // assert

    verify(userRepository, times(1)).findAll();
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);

  }

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_can_toggle_admin_status_of_a_user_from_false_to_true() throws Exception {
          // arrange
          User userBefore = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .admin(false)
          .build();

          User userAfter = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .admin(true)
          .build();

    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.of(userBefore));
          when(userRepository.save(eq(userBefore))).thenReturn(userAfter);
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleAdmin?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isOk()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
          verify(userRepository, times(1)).save(userBefore);

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 has toggled admin status to true", json.get("message"));
  }

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_can_toggle_admin_status_of_a_user_from_true_to_false() throws Exception {
          // arrange
          User userBefore = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .admin(true)
          .build();

          User userAfter = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .admin(false)
          .build();

    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.of(userBefore));
          when(userRepository.save(eq(userBefore))).thenReturn(userAfter);
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleAdmin?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isOk()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
          verify(userRepository, times(1)).save(userBefore);

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 has toggled admin status to false", json.get("message"));
  }

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_tries_to_toggleAdmin_non_existant_user_and_gets_right_error_message() throws Exception {
          // arrange
        
    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.empty());
          
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleAdmin?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isNotFound()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
         

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 not found", json.get("message"));
  }

  // instructor toggle tests

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_can_toggle_instructor_status_of_a_user_from_false_to_true() throws Exception {
          // arrange
          User userBefore = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .instructor(false)
          .build();

          User userAfter = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .instructor(true)
          .build();
    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.of(userBefore));
          when(userRepository.save(eq(userBefore))).thenReturn(userAfter);
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleInstructor?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isOk()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
          verify(userRepository, times(1)).save(userBefore);

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 has toggled instructor status to true", json.get("message"));
  }

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_can_toggle_instructor_status_of_a_user_from_true_to_false() throws Exception {
          // arrange
         User userBefore = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .instructor(true)
          .build();

          User userAfter = User.builder()
          .email("cgaucho@ucsb.edu")
          .githubId(15)
          .instructor(false)
          .build();

    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.of(userBefore));
          when(userRepository.save(eq(userBefore))).thenReturn(userAfter);
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleInstructor?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isOk()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
          verify(userRepository, times(1)).save(userBefore);

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 has toggled instructor status to false", json.get("message"));
  }

  @WithMockUser(roles = { "ADMIN", "USER" })
  @Test
  public void admin_tries_to_toggle_instructor_for_non_existant_user_and_gets_right_error_message() throws Exception {

          // arrange
        
    
          when(userRepository.findByGithubId(15)).thenReturn(Optional.empty());
          
          // act
          MvcResult response = mockMvc.perform(
                          post("/api/admin/users/toggleInstructor?githubId=15")
                                          .with(csrf()))
                          .andExpect(status().isNotFound()).andReturn();

          // assert
          verify(userRepository, times(1)).findByGithubId(15);
         

          Map<String, Object> json = responseToJson(response);
          assertEquals("User with id 15 not found", json.get("message"));
  }
}
