package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuItemRepository ucsbDiningMenuItemRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdiningcommonsmenuitem/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbMenuItem2s() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem ucsbMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();


                UCSBDiningCommonsMenuItem ucsbMenuItem2 = UCSBDiningCommonsMenuItem.builder()
                                .name("lastDayOfClasses")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItems = new ArrayList<>();
                expectedMenuItems.addAll(Arrays.asList(ucsbMenuItem1, ucsbMenuItem2));

                when(ucsbDiningMenuItemRepository.findAll()).thenReturn(expectedMenuItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningMenuItemRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMenuItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommonsmenuitem/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbmenuitem() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem ucsbMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                                .name("BakedPestoPastawithChicken")
                                .diningCommonsCode("ortega")
                                .station("EntreeSpecials")
                                .build();

                when(ucsbDiningMenuItemRepository.save(eq(ucsbMenuItem1))).thenReturn(ucsbMenuItem1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenuitem/post?name=BakedPestoPastawithChicken&diningCommonsCode=ortega&station=EntreeSpecials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningMenuItemRepository, times(1)).save(ucsbMenuItem1);
                String expectedJson = mapper.writeValueAsString(ucsbMenuItem1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsbdiningcommonsmenuitem?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem ucsbMenuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbMenuItem));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningMenuItemRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbMenuItem);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningMenuItemRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningMenuItemRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItem with id 7 not found", json.get("message"));
        }


        // Tests for DELETE /api/ucsbdiningcommonsmenuitem?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_menu_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem ucsbMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningMenuItemRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbMenuItem1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningMenuItemRepository, times(1)).findById(15L);
                verify(ucsbDiningMenuItemRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbmenuitem_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDiningMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningMenuItemRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbdiningcommonsmenuitem?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbmenuitem() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem ucsbMenuItemOrig = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                UCSBDiningCommonsMenuItem ucsbMenuItemEdited = UCSBDiningCommonsMenuItem.builder()
                                .name("Tofu Banh Mi Sandwich (v)")
                                .diningCommonsCode("portola")
                                .station("Lunch")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbMenuItemEdited);

                when(ucsbDiningMenuItemRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbMenuItemOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningMenuItemRepository, times(1)).findById(67L);
                verify(ucsbDiningMenuItemRepository, times(1)).save(ucsbMenuItemEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbmenuitem_that_does_not_exist() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem ucsbEditedMenuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedMenuItem);

                when(ucsbDiningMenuItemRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningMenuItemRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));

        }
}
