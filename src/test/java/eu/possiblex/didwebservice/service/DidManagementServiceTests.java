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
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = { DidManagementServiceTests.TestConfig.class, DidManagementServiceImpl.class,
    DidWebServiceApplication.class })
@Transactional
class DidManagementServiceTests {

    @Autowired
    private DidManagementService sut;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @Value("${did-web-domain}")
    private String didDomain;

    @Value("${common-verification-method.id}")
    private String commonVerificationMethodId;

    @Captor
    private ArgumentCaptor<ParticipantDidDataEntity> certificateArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> didStringArgumentCaptor;

    @Test
    void generateDidWithoutOptionalsCorrectly() {

        String didRegex = "did:web:" + didDomain.replaceFirst(":", "%3A") + ":participant:[-A-Za-z0-9]*";

        ParticipantDidCreateRequestTo request = new ParticipantDidCreateRequestTo();
        request.setSubject("ABC Company");

        ParticipantDidTo dto = sut.generateParticipantDidWeb(request);

        assertTrue(dto.getDid().matches(didRegex));

        verify(participantDidDataRepository).save(certificateArgumentCaptor.capture());
        ParticipantDidDataEntity participant = certificateArgumentCaptor.getValue();

        assertTrue(participant.getDid().matches(didRegex));
    }

    @Test
    void generateDidWithOptionalsCorrectly() {

        String didRegex = "did:web:" + didDomain.replaceFirst(":", "%3A") + ":participant:[-A-Za-z0-9]*";

        ParticipantDidCreateRequestTo request = new ParticipantDidCreateRequestTo();
        request.setSubject("ABC Company");
        request.setAliases(List.of("alias1", "alias2"));
        request.setCertificates(Map.of("certId", "certContent"));

        ParticipantDidTo dto = sut.generateParticipantDidWeb(request);

        assertTrue(dto.getDid().matches(didRegex));

        verify(participantDidDataRepository).save(certificateArgumentCaptor.capture());
        ParticipantDidDataEntity participant = certificateArgumentCaptor.getValue();

        assertTrue(participant.getDid().matches(didRegex));
        assertIterableEquals(request.getAliases(), participant.getAliases());
        assertIterableEquals(request.getCertificates().keySet(),
            participant.getVerificationMethods().stream().map(VerificationMethodEntity::getCertificateId).toList());
    }

    @Test
    void generateDidCollisionWithCommonId() {

        ParticipantDidCreateRequestTo request = new ParticipantDidCreateRequestTo();
        request.setSubject("ABC Company");
        request.setCertificates(Map.of(commonVerificationMethodId, "certContent"));

        assertThrows(RequestArgumentException.class, () -> sut.generateParticipantDidWeb(request));
    }

    @Test
    void generateDidBadRequest() {

        ParticipantDidCreateRequestTo request = new ParticipantDidCreateRequestTo();
        request.setSubject(null);
        request.setCertificates(Map.of("certId", "certContent"));

        assertThrows(RequestArgumentException.class, () -> sut.generateParticipantDidWeb(request));
    }

    @Test
    void deleteExistingDidCorrectly() {

        String did = "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c";
        sut.generateParticipantDidWeb(
            new ParticipantDidCreateRequestTo("some subject", Collections.emptyList(), Collections.emptyMap()));

        sut.removeParticipantDidWeb(did);

        verify(participantDidDataRepository).deleteByDid(didStringArgumentCaptor.capture());
        String didString = didStringArgumentCaptor.getValue();

        assertTrue(did.matches(didString));
    }

    @Test
    void deleteNonExistingDid() {

        sut.generateParticipantDidWeb(
            new ParticipantDidCreateRequestTo("some subject", Collections.emptyList(), Collections.emptyMap()));

        when(participantDidDataRepository.findByDid(any())).thenReturn(null);

        sut.removeParticipantDidWeb("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");

        verify(participantDidDataRepository, never()).deleteByDid(any());
    }

    @Test
    void deleteDidBadRequest() {

        assertThrows(RequestArgumentException.class, () -> sut.removeParticipantDidWeb(""));
    }

    @Test
    void updateExistingDidCorrectly() {

        sut.generateParticipantDidWeb(
            new ParticipantDidCreateRequestTo("some subject", Collections.emptyList(), Collections.emptyMap()));

        Map<String, String> certificates = Map.of("key1", "value1");
        List<String> aliases = List.of("alias1", "alias2");

        ParticipantDidUpdateRequestTo request = new ParticipantDidUpdateRequestTo();
        request.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");
        request.setCertificates(certificates);
        request.setAliases(aliases);

        sut.updateParticipantDidWeb(request);

        ParticipantDidDataEntity entity = participantDidDataRepository.findByDid(
            "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");

        assertIterableEquals(aliases, entity.getAliases());
        assertIterableEquals(certificates.keySet(),
            entity.getVerificationMethods().stream().map(VerificationMethodEntity::getCertificateId).toList());
    }

    @Test
    void updateNonExistingDid() {

        ParticipantDidUpdateRequestTo request = new ParticipantDidUpdateRequestTo();
        request.setDid("did:web:localhost%3A8443:1234");
        request.setAliases(List.of("alias1", "alias2"));

        assertThrows(ParticipantNotFoundException.class, () -> sut.updateParticipantDidWeb(request));
    }

    @Test
    void updateDidBadRequest() {

        ParticipantDidUpdateRequestTo request = new ParticipantDidUpdateRequestTo();
        request.setDid(null);

        assertThrows(RequestArgumentException.class, () -> sut.updateParticipantDidWeb(request));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DidWebUrlService didWebUrlService() {

            return Mockito.spy(new DidWebUrlServiceFake("localhost:8443"));
        }

        @Bean
        public CertificateService certificateService() {

            return Mockito.spy(new CertificateServiceFake());
        }
    }
}
