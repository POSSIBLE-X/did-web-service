/*
 *  Copyright 2024-2025 Dataport. All rights reserved. Developed as part of the POSSIBLE project.
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

package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.DidWebServiceApplication;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.CertificateNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = { CertificateServiceTests.TestConfig.class, CertificateServiceImpl.class,
    DidWebServiceApplication.class })
@Transactional
class CertificateServiceTests {
    @Autowired
    private CertificateService sut;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @BeforeEach
    void setUp() {

        storeExampleParticipant();
    }

    // get certificate for non existing participant
    @Test
    void getCertificateForNonExistingParticipant() {

        assertThrows(ParticipantNotFoundException.class,
            () -> sut.getParticipantCertificate("unknown-participant", "unknown-certificate"));
    }

    // get existing certificate for existing participant
    @Test
    void getCertificateForExistingParticipant() {

        String cert = sut.getParticipantCertificate("existing-participant", "existing-certificate");
        assertNotNull(cert);
    }

    // get non-existing certificate for existing participant
    @Test
    void getNonExistingCertificateForExistingParticipant() {

        assertThrows(CertificateNotFoundException.class,
            () -> sut.getParticipantCertificate("existing-participant", "unknown-certificate"));
    }

    @Test
    void getCommonCertificate() {

        String commonCert = sut.getCommonCertificate();
        assertNotNull(commonCert);
    }

    @Test
    void convertCertificateSuccessfully() throws CertificateException {

        X509Certificate cert = sut.convertPemStringToCertificate(CertificateServiceFake.EXAMPLE_CERTIFICATE);
        assertNotNull(cert);
    }

    @Test
    void convertCertificateBadInput() {

        assertThrows(CertificateException.class, () -> sut.convertPemStringToCertificate("garbage"));
    }

    private void storeExampleParticipant() {

        VerificationMethodEntity vmEntity = new VerificationMethodEntity(null, "existing-certificate", "certificate");
        ParticipantDidDataEntity participantEntity = new ParticipantDidDataEntity(null,
            "did:web:localhost%3A8443:participant:existing-participant", List.of(vmEntity), Collections.emptyList());
        participantDidDataRepository.save(participantEntity);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DidWebUrlService didWebUrlService() {

            return Mockito.spy(new DidWebUrlServiceFake("localhost:8443"));
        }
    }
}
