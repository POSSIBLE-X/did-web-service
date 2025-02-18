/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *  Copyright 2024-2025 Dataport. All rights reserved. Extended as part of the POSSIBLE project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.possiblex.didwebservice.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.possiblex.didwebservice.config.BoundaryExceptionHandler;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.service.DidManagementService;
import eu.possiblex.didwebservice.service.DidManagementServiceFake;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ InternalControllerImpl.class })
@ContextConfiguration(classes = { InternalControllerImpl.class, BoundaryExceptionHandler.class,
    InternalControllerTests.TestConfig.class })
@AutoConfigureMockMvc
class InternalControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void generateDidOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post("/internal/didweb").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getValidCreateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void generateDidBadRequest() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post("/internal/didweb").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getEmptyCreateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateDidOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.patch("/internal/didweb").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getValidUpdateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void updateDidBadRequest() throws Exception {

        ParticipantDidUpdateRequestTo to = getValidUpdateRequest();
        to.setDid(null);

        mvc.perform(MockMvcRequestBuilders.patch("/internal/didweb").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(to)).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateDidNotFound() throws Exception {

        ParticipantDidUpdateRequestTo to = getValidUpdateRequest();
        to.setDid("did:web:example.com:participant:unknown");

        mvc.perform(MockMvcRequestBuilders.patch("/internal/didweb").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(to)).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteDidOkay() throws Exception {

        mvc.perform(MockMvcRequestBuilders.delete("/internal/didweb/123").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getValidUpdateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk());
    }

    private ParticipantDidCreateRequestTo getEmptyCreateRequest() {

        return new ParticipantDidCreateRequestTo();
    }

    private ParticipantDidCreateRequestTo getValidCreateRequest() {

        ParticipantDidCreateRequestTo to = new ParticipantDidCreateRequestTo();
        to.setSubject("valid");
        return to;
    }

    private ParticipantDidUpdateRequestTo getValidUpdateRequest() {

        ParticipantDidUpdateRequestTo to = new ParticipantDidUpdateRequestTo();
        to.setDid("did:web:example.com:participant:123");
        to.setAliases(List.of("alias1", "alias2"));
        return to;
    }

    private String objectAsJsonString(final Object obj) {

        try {
            return JsonMapper.builder().addModule(new JavaTimeModule()).build().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DidManagementService didManagementService() {

            return Mockito.spy(new DidManagementServiceFake());
        }

    }
}
