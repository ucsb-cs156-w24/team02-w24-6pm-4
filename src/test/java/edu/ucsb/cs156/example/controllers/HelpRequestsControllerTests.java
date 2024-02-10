package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDatesController.class)
@Import(TestConfig.class)
public class HelpRequestsControllerTests extends ControllerTestCase {

        @MockBean
        HelpRequestRepository helpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/helprequests/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_helprequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");

                HelpRequest helpRequest1 = HelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-20T18:31");

                HelpRequest helpRequest2 = HelpRequest.builder()
                                .requesterEmail("delplaya@ucsb.edu")
                                .teamId("s22-6pm-3")
                                .tableOrBreakoutRoom("11")
                                .explanation("Dokku problems")
                                .solved(false)
                                .requestTime(ldt2)
                                .build();

                ArrayList<HelpRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

                when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/helprequests/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequests/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequests/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");

                HelpRequest helpRequest1 = HelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Swagger")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/helprequests/post?requesterEmail=firstDayOfClasses&teamId=s22-5pm-3&tableOrBreakoutRoom=7&explanation=Swagger&solved=false&requestTime=2022-04-20T17:35")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(helpRequest1);
                String expectedJson = mapper.writeValueAsString(helpRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsbdates?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdates?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBDate ucsbDate = UCSBDate.builder()
                                .name("firstDayOfClasses")
                                .quarterYYYYQ("20222")
                                .localDateTime(ldt)
                                .build();

                when(ucsbDateRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbDate));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdates?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDateRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbDate);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDateRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdates?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDateRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDate with id 7 not found", json.get("message"));
        }


        // Tests for DELETE /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBDate ucsbDate1 = UCSBDate.builder()
                                .name("firstDayOfClasses")
                                .quarterYYYYQ("20222")
                                .localDateTime(ldt1)
                                .build();

                when(ucsbDateRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbDate1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdates?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDateRepository, times(1)).findById(15L);
                verify(ucsbDateRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDate with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDateRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdates?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDateRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDate with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                UCSBDate ucsbDateOrig = UCSBDate.builder()
                                .name("firstDayOfClasses")
                                .quarterYYYYQ("20222")
                                .localDateTime(ldt1)
                                .build();

                UCSBDate ucsbDateEdited = UCSBDate.builder()
                                .name("firstDayOfFestivus")
                                .quarterYYYYQ("20232")
                                .localDateTime(ldt2)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbDateEdited);

                when(ucsbDateRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbDateOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdates?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDateRepository, times(1)).findById(67L);
                verify(ucsbDateRepository, times(1)).save(ucsbDateEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBDate ucsbEditedDate = UCSBDate.builder()
                                .name("firstDayOfClasses")
                                .quarterYYYYQ("20222")
                                .localDateTime(ldt1)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(ucsbDateRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdates?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDateRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDate with id 67 not found", json.get("message"));

        }
}
