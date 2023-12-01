package edu.ucsb.cs156.organic.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.awaitility.Awaitility.await;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.checkerframework.checker.units.qual.Current;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import edu.ucsb.cs156.organic.entities.Course;
import edu.ucsb.cs156.organic.entities.Staff;
import edu.ucsb.cs156.organic.entities.User;
import edu.ucsb.cs156.organic.entities.jobs.Job;
import edu.ucsb.cs156.organic.models.CurrentUser;
import edu.ucsb.cs156.organic.repositories.CourseRepository;
import edu.ucsb.cs156.organic.repositories.StaffRepository;
import edu.ucsb.cs156.organic.repositories.UserRepository;
import edu.ucsb.cs156.organic.repositories.jobs.JobsRepository;
import edu.ucsb.cs156.organic.services.CurrentUserService;
import edu.ucsb.cs156.organic.services.jobs.JobService;
import lombok.extern.slf4j.Slf4j;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

@Slf4j
@WebMvcTest(controllers = CoursesController.class)
@Import(JobService.class)
@AutoConfigureDataJpa
public class CoursesControllerTests extends ControllerTestCase {

        @MockBean
        UserRepository userRepository;

        @MockBean
        CourseRepository courseRepository;

        @MockBean
        StaffRepository courseStaffRepository;

        @Autowired
        ObjectMapper objectMapper;

        Course course1 = Course.builder()
                        .id(1L)
                        .name("CS156")
                        .school("UCSB")
                        .term("F23")
                        .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                        .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                        .githubOrg("ucsb-cs156-f23")
                        .build();

        Course course2 = Course.builder()
                        .id(1L)
                        .name("CS148")
                        .school("UCSB")
                        .term("S24")
                        .startDate(LocalDateTime.parse("2024-01-01T00:00:00"))
                        .endDate(LocalDateTime.parse("2024-03-31T00:00:00"))
                        .githubOrg("ucsb-cs148-w24")
                        .build();

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void admin_can_get_all_courses() throws Exception {

                // arrange

                ArrayList<Course> expectedCourses = new ArrayList<>();
                expectedCourses.addAll(Arrays.asList(course1, course2));

                when(courseRepository.findAll()).thenReturn(expectedCourses);

                // act
                MvcResult response = mockMvc.perform(get("/api/courses/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseRepository, atLeastOnce()).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCourses);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void user_can_get_only_courses_for_which_they_are_staff() throws Exception {

                // arrange

                ArrayList<Course> expectedCourses = new ArrayList<>();
                expectedCourses.addAll(Arrays.asList(course1, course2));

                when(courseRepository.findCoursesStaffedByUser(any())).thenReturn(expectedCourses);

                // act
                MvcResult response = mockMvc.perform(get("/api/courses/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseRepository, atLeastOnce()).findCoursesStaffedByUser(any());
                String expectedJson = mapper.writeValueAsString(expectedCourses);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_course() throws Exception {
                // arrange

                Course courseBefore = Course.builder()
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                Course courseAfter = Course.builder()
                                .id(222L)
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                when(courseRepository.save(eq(courseBefore))).thenReturn(courseAfter);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/courses/post?name=CS16&school=UCSB&term=F23&startDate=2023-09-01T00:00:00&endDate=2023-12-31T00:00:00&githubOrg=ucsb-cs16-f23")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).save(courseBefore);
                String expectedJson = mapper.writeValueAsString(courseAfter);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_can_update_a_course() throws Exception {
                // arrange

                Course courseOrig = Course.builder()
                                .id(123L)
                                .name("CS123")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-02T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-30T00:00:00"))
                                .githubOrg("ucsb-cs123-f23")
                                .build();

                Course courseEdited = Course.builder()
                                .id(123L)
                                .name("CS124")
                                .school("UCSBSB")
                                .term("W24")
                                .startDate(LocalDateTime.parse("2024-01-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2024-03-31T00:00:00"))
                                .githubOrg("ucsb-cs123-w24")
                                .build();

                String requestBody = mapper.writeValueAsString(courseEdited);

                when(courseRepository.findById(eq(123L))).thenReturn(Optional.of(courseOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/courses/update?id=123")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                verify(courseRepository, times(1)).save(courseEdited);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "INSTRUCTOR" })
        @Test
        public void an_instructor_user_can_update_a_course() throws Exception {
                // arrange

                User user = User.builder()
                        .githubId(12345)
                        .githubNodeId("abc")
                        .githubLogin("testuser123")
                        .email("testuser@gmail.com")
                        .pictureUrl("pic")
                        .fullName("test user")
                        .emailVerified(true)
                        .admin(false)
                        .instructor(true)
                        .accessToken("access")
                        .build();
                
                Course courseOrig = Course.builder()
                                .id(123L)
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                Course courseEdited = Course.builder()
                                .id(123L)
                                .name("CS24")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs24-f23")
                                .build();
                
                Staff staff = Staff.builder()
                        .id(123L)
                        .courseId(123L)
                        .githubId(12345)
                        .build();

                String requestBody = mapper.writeValueAsString(courseEdited);

                when(courseRepository.findById(eq(123L))).thenReturn(Optional.of(courseOrig));
                when(courseStaffRepository.findByCourseIdAndGithubId(courseOrig.getId(), user.getGithubId())).thenReturn(Optional.of(staff));


                // act
                
                MvcResult response = mockMvc.perform(
                                put("/api/courses/update?id=123")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                verify(courseRepository, times(1)).save(courseEdited);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "INSTRUCTOR" })
        @Test
        public void a_non_admin_or_instructor_user_cannot_update_a_course() throws Exception {
                // arrange

                Course courseOrig = Course.builder()
                                .id(123L)
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                Course courseEdited = Course.builder()
                                .id(123L)
                                .name("CS24")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs24-f23")
                                .build();

                String requestBody = mapper.writeValueAsString(courseEdited);

                when(courseRepository.findById(eq(courseOrig.getId()))).thenReturn(Optional.of(courseOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/courses/update?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Staff with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_cannot_update_nonexistent_course() throws Exception {
                // arrange

                Course courseEdited = Course.builder()
                                .name("CS24")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs24-f23")
                                .build();

                String requestBody = mapper.writeValueAsString(courseEdited);

                when(courseRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/courses/update?id=1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_can_delete_a_course() throws Exception {
                // arrange

                Course course = Course.builder()
                                .id(123L)
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                when(courseRepository.findById(eq(123L))).thenReturn(Optional.of(course));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/courses/delete?id=123")
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                verify(courseRepository, times(1)).delete(course);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 123 deleted.", json.get("message"));
        }

        @WithMockUser(roles = { "INSTRUCTOR" })
        @Test
        public void an_instructor_user_can_delete_a_course() throws Exception {
                // arrange

                User user = User.builder()
                        .githubId(12345)
                        .githubNodeId("abc")
                        .githubLogin("testuser123")
                        .email("testuser@gmail.com")
                        .pictureUrl("pic")
                        .fullName("test user")
                        .emailVerified(true)
                        .admin(false)
                        .instructor(true)
                        .accessToken("access")
                        .build();
                
                Course course = Course.builder()
                        .id(123L)
                        .name("CS16")
                        .school("UCSB")
                        .term("F23")
                        .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                        .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                        .githubOrg("ucsb-cs16-f23")
                        .build();
                
                Staff staff = Staff.builder()
                        .id(123L)
                        .courseId(123L)
                        .githubId(12345)
                        .build();
                
                when(courseRepository.findById(eq(123L))).thenReturn(Optional.of(course));
                when(courseStaffRepository.findByCourseIdAndGithubId(course.getId(), user.getGithubId())).thenReturn(Optional.of(staff));

                // act
                
                MvcResult response = mockMvc.perform(
                                delete("/api/courses/delete?id=123")
                                        .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                verify(courseRepository, times(1)).delete(course);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 123 deleted.", json.get("message"));
        }

        @WithMockUser(roles = { "INSTRUCTOR" })
        @Test
        public void a_non_admin_or_instructor_user_cannot_delete_a_course() throws Exception {
                // arrange

                Course course = Course.builder()
                                .id(123L)
                                .name("CS16")
                                .school("UCSB")
                                .term("F23")
                                .startDate(LocalDateTime.parse("2023-09-01T00:00:00"))
                                .endDate(LocalDateTime.parse("2023-12-31T00:00:00"))
                                .githubOrg("ucsb-cs16-f23")
                                .build();

                when(courseRepository.findById(eq(course.getId()))).thenReturn(Optional.of(course));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/courses/delete?id=123")
                                        .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Staff with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void an_admin_user_cannot_delete_nonexistent_course() throws Exception {
                // arrange

                when(courseRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/courses/delete?id=123")
                                        .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_add_a_staff_member_to_a_course() throws Exception {
                // arrange

                User user = User.builder().githubId(12345).githubLogin("scottpchow23").build();

                Staff courseStaffBefore = Staff.builder()
                                .courseId(course1.getId())
                                .githubId(user.getGithubId())
                                .user(user)
                                .build();

                Staff courseStaffAfter = Staff.builder()
                                .id(456L)
                                .courseId(course1.getId())
                                .githubId(user.getGithubId())
                                .user(user)
                                .build();

                when(courseRepository.findById(eq(course1.getId()))).thenReturn(Optional.of(course1));
                when(userRepository.findByGithubLogin(eq("scottpchow23"))).thenReturn(Optional.of(user));
                when(courseStaffRepository.save(eq(courseStaffBefore))).thenReturn(courseStaffAfter);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/courses/addStaff?courseId=1&githubLogin=scottpchow23")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseStaffRepository, times(1)).save(courseStaffBefore);
                String expectedJson = mapper.writeValueAsString(courseStaffAfter);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_cannot_add_staff_to_a_non_existing_course() throws Exception {
                // arrange

                when(courseRepository.findById(eq(42L))).thenReturn(Optional.empty());
                // act

                MvcResult response = mockMvc.perform(
                                post("/api/courses/addStaff?courseId=42&githubLogin=scottpchow23")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                Map<String,String> responseMap = mapper.readValue(response.getResponse().getContentAsString(), new TypeReference<Map<String,String>>(){});
                Map<String,String> expectedMap = Map.of("message", "Course with id 42 not found", "type", "EntityNotFoundException");
                assertEquals(expectedMap, responseMap);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_cannot_add_non_existing_user_to_staff_of_an_existing_course() throws Exception {
                // arrange

                when(courseRepository.findById(eq(course1.getId()))).thenReturn(Optional.of(course1));
                when(userRepository.findByGithubLogin(eq("sadGaucho"))).thenReturn(Optional.empty());
               
                // act

                MvcResult response = mockMvc.perform(
                                post("/api/courses/addStaff?courseId=1&githubLogin=sadGaucho")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                Map<String,String> responseMap = mapper.readValue(response.getResponse().getContentAsString(), new TypeReference<Map<String,String>>(){});
                Map<String,String> expectedMap = Map.of("message", "User with id sadGaucho not found", "type", "EntityNotFoundException");
                assertEquals(expectedMap, responseMap);
        }



        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_get_staff_for_a_course() throws Exception {
                // arrange

                User user1 = User.builder().githubId(12345).githubLogin("scottpchow23").build();
                User user2 = User.builder().githubId(67890).githubLogin("pconrad").build();

                Staff courseStaff1 = Staff.builder()
                                .id(111L)
                                .courseId(course1.getId())
                                .githubId(user1.getGithubId())
                                .user(user1)
                                .build();

                Staff courseStaff2 = Staff.builder()
                                .id(222L)
                                .courseId(course2.getId())
                                .githubId(user2.getGithubId())
                                .user(user2)
                                .build();

                ArrayList<Staff> expectedCourseStaff = new ArrayList<>();
                expectedCourseStaff.addAll(Arrays.asList(courseStaff1, courseStaff2));

                when(courseRepository.findById(eq(course1.getId()))).thenReturn(Optional.of(course1));
                when(courseStaffRepository.findByCourseId(eq(course1.getId()))).thenReturn(expectedCourseStaff);

                // act

                MvcResult response = mockMvc.perform(
                                get("/api/courses/getStaff?courseId=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseStaffRepository, times(1)).findByCourseId(eq(course1.getId()));
                String expectedJson = mapper.writeValueAsString(expectedCourseStaff);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_cannot_get_staff_for_a_non_existing_course() throws Exception {
                // arrange

                when(courseRepository.findById(eq(42L))).thenReturn(Optional.empty());
                // act

                MvcResult response = mockMvc.perform(
                                get("/api/courses/getStaff?courseId=42")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                Map<String,String> responseMap = mapper.readValue(response.getResponse().getContentAsString(), new TypeReference<Map<String,String>>(){});
                Map<String,String> expectedMap = Map.of("message", "Course with id 42 not found", "type", "EntityNotFoundException");
                assertEquals(expectedMap, responseMap);
        }


        // TESTS FOR GETTING A SINGLE COURSE BY ID

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/courses?id=42"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_get_course_by_id_for_existing_id() throws Exception {
                // arrange
                when(courseRepository.findById(eq(42L))).thenReturn(Optional.of(course1));

                // act
                MvcResult response = mockMvc.perform(get("/api/courses?id=42"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseRepository, times(1)).findById(eq(42L));
                String expectedJson = mapper.writeValueAsString(course1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);

        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_cannot_get_course_for_a_non_existing_id() throws Exception {
                // arrange

                when(courseRepository.findById(eq(42L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                get("/api/courses?id=42"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(42L);
                Map<String,Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Course with id 42 not found", json.get("message"));

        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void a_user_can_get_course_if_on_course_staff() throws Exception {
                // arrange
                User currentUser = currentUserService.getCurrentUser().getUser();

                Staff courseStaff1 = Staff.builder()
                                .id(111L)
                                .courseId(course1.getId())
                                .githubId(currentUser.getGithubId())
                                .user(currentUser)
                                .build();

                when(courseRepository.findById(eq(course1.getId()))).thenReturn(Optional.of(course1));
                when(courseStaffRepository.findByCourseIdAndGithubId(eq(course1.getId()),
                                eq(courseStaff1.getGithubId()))).thenReturn(Optional.of(courseStaff1));

                // act
                MvcResult response = mockMvc.perform(get("/api/courses?id=1"))
                                .andExpect(status().isOk()).andReturn();


                // assert
                verify(courseRepository, times(1)).findById(eq(1L));
                verify(courseStaffRepository, times(1)).findByCourseIdAndGithubId(eq(course1.getId()),
                                eq(courseStaff1.getGithubId()));
                String expectedJson = mapper.writeValueAsString(course1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        @WithMockUser(roles = { "USER" })
        @Test
        public void a_user_cannot_get_course_if_not_on_course_staff() throws Exception {
                // arrange
                User currentUser = currentUserService.getCurrentUser().getUser();

                when(courseRepository.findById(eq(course1.getId()))).thenReturn(Optional.of(course1));

                // act
                MvcResult response = mockMvc.perform(get("/api/courses?id=1"))
                                .andExpect(status().isForbidden()).andReturn();


                // assert
                verify(courseRepository, times(1)).findById(eq(1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("AccessDeniedException", json.get("type"));
                assertEquals(String.format("User %s is not authorized to get course 1", currentUser.getGithubLogin()), json.get("message"));
        }

        
}
