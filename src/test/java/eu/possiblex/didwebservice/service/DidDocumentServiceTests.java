package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.DidWebServiceApplication;
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = { DidDocumentServiceTests.TestConfig.class, DidDocumentServiceImpl.class,
    DidWebServiceApplication.class })
class DidDocumentServiceTests {

    @Autowired
    private DidDocumentService sut;

    @Value("${common-verification-method.id}")
    private String commonVerificationMethodId;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @Test
    void getCommonDidDocument() {

        DidDocument commonDidDocument = sut.getCommonDidDocument();
        assertNotNull(commonDidDocument);
    }

    @Test
    void getParticipantDidDocumentCorrectly() {

        ParticipantDidDataEntity participantDidDataEntity = getTestParticipantCertificate();
        List<String> vmIdsWithCommonVm = new ArrayList<>(
            participantDidDataEntity.getVerificationMethods().stream().map(VerificationMethodEntity::getCertificateId)
                .toList());
        vmIdsWithCommonVm.add(participantDidDataEntity.getDid() + "#" + commonVerificationMethodId);
        participantDidDataRepository.save(participantDidDataEntity);

        DidDocument actual = sut.getParticipantDidDocument("c0334816-5608-387d-b935-7894158d4b1c");

        assertEquals(actual.getId(), participantDidDataEntity.getDid());
        assertIterableEquals(actual.getAlsoKnownAs(), participantDidDataEntity.getAliases());
        assertIterableEquals(vmIdsWithCommonVm,
            actual.getVerificationMethod().stream().map(VerificationMethod::getId).toList());
    }

    private ParticipantDidDataEntity getTestParticipantCertificate() {

        ParticipantDidDataEntity participantDidDataEntity = new ParticipantDidDataEntity();
        participantDidDataEntity.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");
        participantDidDataEntity.setVerificationMethods(Collections.emptyList());
        participantDidDataEntity.setAliases(Collections.emptyList());
        return participantDidDataEntity;
    }

    private String getTestDidDocumentJsonString() {

        return """
            {
                "@context": [
                    "https://www.w3.org/ns/did/v1",
                    "https://w3id.org/security/suites/jws-2020/v1"
                ],
                "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c",
                "alsoKnownAs": [],
                "verificationMethod": [
                    {
                        "@context": [
                            "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/"
                        ],
                        "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c#some-id",
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

    private String getTestDidDocumentJsonStringWithAliases() {

        return """
            {
                "@context": [
                    "https://www.w3.org/ns/did/v1",
                    "https://w3id.org/security/suites/jws-2020/v1"
                ],
                "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c",
                "alsoKnownAs": [
                    "alias1",
                    "alias2"
                ],
                "verificationMethod": [
                    {
                        "@context": [
                            "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/"
                        ],
                        "id": "did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c#some-id",
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
