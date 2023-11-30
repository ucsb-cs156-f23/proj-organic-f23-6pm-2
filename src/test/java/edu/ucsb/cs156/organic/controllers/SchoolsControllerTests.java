package edu.ucsb.cs156.organic.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.awaitility.Awaitility.await;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import edu.ucsb.cs156.organic.entities.Course;
import edu.ucsb.cs156.organic.entities.School;
import edu.ucsb.cs156.organic.repositories.SchoolRepository;
import edu.ucsb.cs156.organic.services.jobs.JobService;

@WebMvcTest(controllers = SchoolsController.class)
@Import(JobService.class)
@AutoConfigureDataJpa
public class SchoolsControllerTests extends ControllerTestCase {

        @MockBean
        SchoolRepository schoolRepository;

        @Autowired
        ObjectMapper objectMapper;

        School school1 = School.builder()
                        .abbrev("ucsb")
                        .name("UC Santa Barbara")
                        .termRegex("[WSMF]\\d\\d")
                        .termDescription("F23")
                        .termError("error")
                        .build();

        School school2 = School.builder()
                        .abbrev("UCSB")
                        .name("University of California - Santa Barbara")
                        .termRegex("[FWSM]\\d\\d")
                        .termDescription("W24")
                        .termError("termError")
                        .build();

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void admin_can_get_all_schools() throws Exception {

                // arrange

                ArrayList<School> expectedSchools = new ArrayList<>();
                expectedSchools.addAll(Arrays.asList(school1, school2));

                when(schoolRepository.findAll()).thenReturn(expectedSchools);

                // act
                MvcResult response = mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(schoolRepository, atLeastOnce()).findAll();
                String expectedJson = mapper.writeValueAsString(expectedSchools);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void admin_can_get_by_abbrev_when_abbrev_exists() throws Exception {

                // arrange

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.of(school1));

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?abbrev=ucsb"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(schoolRepository, atLeastOnce()).findByAbbrev(eq(school1.getAbbrev()));
                String expectedJson = mapper.writeValueAsString(school1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void admin_cannot_get_by_abbrev_when_abbrev_does_not_exists() throws Exception {

                // arrange

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?abbrev=ucsb"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(schoolRepository, atLeastOnce()).findByAbbrev(eq(school1.getAbbrev()));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("School with id ucsb not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_can_post_a_new_school() throws Exception {
                // arrange

                when(schoolRepository.save(eq(school1))).thenReturn(school1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/schools/post?abbrev=ucsb&name=UC Santa Barbara&termRegex=[WSMF]\\d\\d&termDescription=F23&termError=error")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolRepository, times(1)).save(school1);
                String expectedJson = mapper.writeValueAsString(school1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_can_update_a_school() throws Exception {
                // arrange

                School school1Edited = School.builder()
                                        .abbrev("ucsb")
                                        .name("UCSB")
                                        .termRegex("[FWSM]\\d\\d")
                                        .termDescription("S23")
                                        .termError("errors")
                                        .build();

                String requestBody = mapper.writeValueAsString(school1Edited);

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.of(school1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools/update?abbrev=ucsb")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                .content(requestBody)
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolRepository, times(1)).findByAbbrev(school1.getAbbrev());
                verify(schoolRepository, times(1)).save(school1Edited);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_cannot_update_a_nonexistent_school() throws Exception {
                // arrange

                School school1Edited = School.builder()
                                        .abbrev("ucsb")
                                        .name("UCSB")
                                        .termRegex("[FWSM]\\d\\d")
                                        .termDescription("S23")
                                        .termError("errors")
                                        .build();

                String requestBody = mapper.writeValueAsString(school1Edited);

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools/update?abbrev=ucsb")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                .content(requestBody)
                                        .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(schoolRepository, atLeastOnce()).findByAbbrev(eq(school1.getAbbrev()));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("School with id ucsb not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_can_delete_a_school() throws Exception {
                // arrange

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.of(school1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools/delete?abbrev=ucsb")
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(schoolRepository, times(1)).findByAbbrev(eq(school1.getAbbrev()));
                verify(schoolRepository, times(1)).delete(school1);
                Map<String, Object> json = responseToJson(response);
                assertEquals("School with abbrev ucsb deleted.", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_cannot_delete_a_nonexistent_school() throws Exception {
                // arrange

                when(schoolRepository.findByAbbrev(eq(school1.getAbbrev()))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools/delete?abbrev=ucsb")
                                        .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(schoolRepository, atLeastOnce()).findByAbbrev(eq(school1.getAbbrev()));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("School with id ucsb not found", json.get("message"));
        }
}
