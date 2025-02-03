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

package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.config.BoundaryExceptionHandler;
import eu.possiblex.didwebservice.service.CertificateService;
import eu.possiblex.didwebservice.service.CertificateServiceFake;
import eu.possiblex.didwebservice.service.DidDocumentService;
import eu.possiblex.didwebservice.service.DidDocumentServiceFake;
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

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ DidControllerImpl.class })
@ContextConfiguration(classes = { DidControllerImpl.class, BoundaryExceptionHandler.class,
    DidControllerTests.TestConfig.class })
@AutoConfigureMockMvc
class DidControllerTests {

    @Autowired
    private MockMvc mvc;

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

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DidDocumentService didDocumentService() {

            return Mockito.spy(new DidDocumentServiceFake());
        }

        @Bean
        public CertificateService certificateService() {

            return Mockito.spy(new CertificateServiceFake());
        }
    }
}
