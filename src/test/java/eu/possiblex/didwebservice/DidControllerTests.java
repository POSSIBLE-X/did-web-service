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

import eu.possiblex.didwebservice.controller.DidControllerImpl;
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.service.CertificateService;
import eu.possiblex.didwebservice.service.DidDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ DidControllerImpl.class })
@AutoConfigureMockMvc
class DidControllerTests {
    @MockBean
    private DidDocumentService didDocumentService;

    @MockBean
    private CertificateService certificateService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void beforeEach() {

        lenient().when(didDocumentService.getParticipantDidDocument(any())).thenReturn(new DidDocument());
        lenient().when(certificateService.getParticipantCertificate(any(), any())).thenReturn("certificate");
        lenient().when(didDocumentService.getParticipantDidDocument("unknown-participant"))
            .thenThrow(ParticipantNotFoundException.class);
        lenient().when(certificateService.getParticipantCertificate(eq("unknown-participant"), any()))
            .thenThrow(ParticipantNotFoundException.class);
        lenient().when(didDocumentService.getParticipantDidDocument("broken-certificate"))
            .thenThrow(DidDocumentGenerationException.class);

    }

    @Test
    void getDidDocumentOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/participant/any/did.json").accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getCommonDidDocumentOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/.well-known/did.json").accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getCommonDidDocumentFailed() throws Exception {

        when(didDocumentService.getCommonDidDocument()).thenThrow(
            new DidDocumentGenerationException("Failed to generate did document"));
        mvc.perform(MockMvcRequestBuilders.get("/.well-known/did.json").accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    void getDidDocumentNotFound() throws Exception {

        mvc.perform(
                MockMvcRequestBuilders.get("/participant/unknown-participant/did.json").accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    void getDidDocumentInternalServerError() throws Exception {

        mvc.perform(
                MockMvcRequestBuilders.get("/participant/broken-certificate/did.json").accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    void getCommonCertificateOk() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/.well-known/cert.ss.pem")
            .accept(MediaType.parseMediaType("application/x-x509-ca-cert"))).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getParticipantCertificateNotFound() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/participant/unknown-participant/cert.ss.pem")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.parseMediaType("application/x-x509-ca-cert"), MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isNotFound());
    }
}
