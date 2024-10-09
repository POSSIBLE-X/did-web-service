/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
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

package eu.possiblex.didwebservice;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.possiblex.didwebservice.controller.DidController;
import eu.possiblex.didwebservice.controller.InternalControllerImpl;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.service.DidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.security.cert.CertificateException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ InternalControllerImpl.class })
@AutoConfigureMockMvc
class InternalControllerTests {
    @MockBean
    private DidService didService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void beforeEach() throws Exception {

        ParticipantDidTo dto = new ParticipantDidTo();
        ParticipantDidCreateRequestTo emptyRequest = getEmptyCreateRequest();

        lenient().when(didService.generateParticipantDidWeb(any())).thenReturn(dto);
        lenient().when(didService.generateParticipantDidWeb(emptyRequest)).thenThrow(RequestArgumentException.class);
    }

    @Test
    void generateDidOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post("/internal/didweb/generate").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getValidCreateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void generateDidBadRequest() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post("/internal/didweb/generate").contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJsonString(getEmptyCreateRequest())).accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isBadRequest());
    }

    private ParticipantDidCreateRequestTo getEmptyCreateRequest() {

        return new ParticipantDidCreateRequestTo();
    }

    private ParticipantDidCreateRequestTo getValidCreateRequest() {
        ParticipantDidCreateRequestTo to = new ParticipantDidCreateRequestTo();
        to.setSubject("valid");
        return to;
    }

    private String objectAsJsonString(final Object obj) {

        try {
            return JsonMapper.builder().addModule(new JavaTimeModule()).build().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
