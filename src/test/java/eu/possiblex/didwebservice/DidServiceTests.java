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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidRemoveRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidData;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import eu.possiblex.didwebservice.service.DidService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableConfigurationProperties
@Transactional
class DidServiceTests {
    @Value("${did-domain}")
    private String didDomain;

    @Autowired
    private DidService didService;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @Captor
    private ArgumentCaptor<ParticipantDidData> certificateArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> didStringArgumentCaptor;

    @BeforeEach
    public void setUp() {

        //ReflectionTestUtils.setField(didService, "participantDidDataRepository", participantDidDataRepository);
    }

    @Test
    void generateDidAndPrivateKeyCorrectly() throws Exception {

        String didRegex = "did:web:" + didDomain.replaceFirst(":", "%3A") + ":participant:[-A-Za-z0-9]*";

        ParticipantDidCreateRequestTo request = new ParticipantDidCreateRequestTo();
        request.setSubject("ABC Company");

        ParticipantDidTo dto = didService.generateParticipantDidWeb(request);

        assertTrue(dto.getDid().matches(didRegex));

        verify(participantDidDataRepository).save(certificateArgumentCaptor.capture());
        ParticipantDidData participant = certificateArgumentCaptor.getValue();

        assertTrue(participant.getDid().matches(didRegex));
    }

    @Test
    void deleteExistingDidCorrectly() throws Exception {

        didService.generateParticipantDidWeb(new ParticipantDidCreateRequestTo("some subject"));

        ParticipantDidRemoveRequestTo request = new ParticipantDidRemoveRequestTo();
        request.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");

        didService.removeParticipantDidWeb(request);

        verify(participantDidDataRepository).deleteByDid(didStringArgumentCaptor.capture());
        String didString = didStringArgumentCaptor.getValue();

        assertTrue(request.getDid().matches(didString));
    }

    @Test
    void deleteNonExistingDid() throws Exception {

        didService.generateParticipantDidWeb(new ParticipantDidCreateRequestTo("some subject"));

        ParticipantDidRemoveRequestTo request = new ParticipantDidRemoveRequestTo();
        request.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");

        when(participantDidDataRepository.findByDid(any())).thenReturn(null);

        didService.removeParticipantDidWeb(request);

        verify(participantDidDataRepository, never()).deleteByDid(any());
    }

    @Test
    void updateExistingDidCorrectly() throws Exception {

        didService.generateParticipantDidWeb(new ParticipantDidCreateRequestTo("some subject"));

        ParticipantDidUpdateRequestTo request = new ParticipantDidUpdateRequestTo();
        request.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");
        request.setAliases(List.of("alias1", "alias2"));

        didService.updateParticipantDidWeb(request);

        ParticipantDidData data = participantDidDataRepository.findByDid(request.getDid());
        assertThat(data.getAliases()).containsExactlyInAnyOrder("alias1", "alias2");
    }

    @Test
    void updateNonExistingDid() throws Exception {

        ParticipantDidUpdateRequestTo request = new ParticipantDidUpdateRequestTo();
        request.setDid("did:web:localhost%3A8443:1234");
        request.setAliases(List.of("alias1", "alias2"));

        assertThrows(ParticipantNotFoundException.class, () -> didService.updateParticipantDidWeb(request));
    }

    @Test
    void getCommonCertificate() throws Exception {

        String commonCert = didService.getCommonCertificate();
        assertNotNull(commonCert);
    }

    @Test
    void getDidDocumentCorrectly() throws Exception {

        didService.generateParticipantDidWeb(new ParticipantDidCreateRequestTo("some subject"));

        ObjectMapper mapper = new ObjectMapper();

        String expectedJsonString = getTestDidDocumentJsonString();
        DidDocument expected = mapper.readValue(expectedJsonString, DidDocument.class);

        String actualJsonString = didService.getDidDocument("c0334816-5608-387d-b935-7894158d4b1c");
        DidDocument actual = mapper.readValue(actualJsonString, DidDocument.class);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void getCommonDidDocument() throws Exception {

        String commonDidDocument = didService.getCommonDidDocument();
        assertNotNull(commonDidDocument);
    }

    private ParticipantDidData getTestParticipantCertificate() {

        ParticipantDidData participantDidData = new ParticipantDidData();
        participantDidData.setDid("did:web:localhost%3A8443:participant:46fa1bd9-3eb6-492f-84a0-5f78a42065b3");
        return participantDidData;
    }

    private String getTestDidDocumentJsonString() {

        return """
            {
                "@context": [
                    "https://www.w3.org/ns/did/v1",
                    "https://w3id.org/security/suites/jws-2020/v1"
                ],
                "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c",
                "verificationMethod": [
                    {
                        "@context": [
                            "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/"
                        ],
                        "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c#JWK2020-PossibleLetsEncrypt",
                        "type": "JsonWebKey2020",
                        "controller": "did:web:localhost%3A8443",
                        "publicKeyJwk": {
                            "kty": "RSA",
                            "n": "ANJ2GVOhLrsxygQs5HAWarDJFWV54GDu1bo3y1P-MrO6JxeB8UyTz9zhihI242zIJqWu7ymlkaJrf11043pgN693-bfG49CKKhX720yKuuRlCCIeMtplW6JnXEC0StgLn-_bw4qojjZJ00rLaD4wIgoOres_yq7hhWWwzoWJGcKq4xp5gfy3xUpaXi8JEEPuXVS4YV5CJploZwAqAKPBAp8tuAKe8C2zfYvaNXzUs9rrMwAo9M8RYZdzRrpxxVJt2JBndFEb6E6F6SvWuM34oUlMR43k9P-2vablReBN8NQAI0oeJ1d6SxNHCcgyE1W9jOHd5vbY48_918I2IgACdTClQUigzNu6XsURQiY_w72_na_gCJoagYTwx5_4I3WkWSFaAAwuM8AVC5Kb1GlCCpjRcmDow2Flkwc03-BrPUC-WnZVX1citeDGTwTsqvnKiCMpoKegOf0d4SpwggT_Av0tPlQ4nYSOj6-VST8fQ8nSNHgdg4jsjmb234O7ClZCVxVBCUYgUzIbo8o2Knk7Qh4whR3LWVUPIVNu_XspO5qZqQ65LXwhSRYvtNGc0Fk4LcwaBoZHuYY9IY7RtZ-IzegX8qXU-aAfg3l5dj9Yaf4TQvSOYL3llGBwKjeFSr3v-dgN7m_LwZSEkIRFHmaBVLXq04gwNzciu8LI_1e_ijOl",
                            "e": "AQAB",
                            "alg": "PS256",
                            "x5u": "https://localhost:8443/.well-known/cert.ss.pem"
                        }
                    }
                ]
            }""";
    }
}
