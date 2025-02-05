package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.DidWebServiceApplication;
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = { DidDocumentServiceTests.TestConfig.class, DidDocumentServiceImpl.class,
    DidWebServiceApplication.class })
@Transactional
class DidDocumentServiceTests {

    @Autowired
    private DidDocumentService sut;

    @Value("${common-verification-method.id}")
    private String commonVerificationMethodId;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @Autowired
    private CertificateService certificateService;

    @Test
    void getCommonDidDocument() {

        DidDocument commonDidDocument = sut.getCommonDidDocument();
        assertNotNull(commonDidDocument);
    }

    @Test
    void getParticipantDidDocumentCorrectly() {

        ParticipantDidDataEntity participantDidDataEntity = getTestParticipantCertificate();
        participantDidDataEntity.setAliases(List.of("alias1", "alias2"));
        participantDidDataEntity.setVerificationMethods(List.of(new VerificationMethodEntity(null, "vm1", "cert1"),
            new VerificationMethodEntity(null, "vm2", "cert2")));
        List<String> vmIdsWithCommonVm = new ArrayList<>(participantDidDataEntity.getVerificationMethods().stream()
            .map(vm -> participantDidDataEntity.getDid() + "#" + vm.getCertificateId()).toList());
        vmIdsWithCommonVm.add(participantDidDataEntity.getDid() + "#" + commonVerificationMethodId);
        participantDidDataRepository.save(participantDidDataEntity);

        DidDocument actual = sut.getParticipantDidDocument("c0334816-5608-387d-b935-7894158d4b1c");

        assertEquals(actual.getId(), participantDidDataEntity.getDid());
        assertIterableEquals(actual.getAlsoKnownAs(), participantDidDataEntity.getAliases());
        assertThat(vmIdsWithCommonVm).containsExactlyInAnyOrderElementsOf(
            actual.getVerificationMethod().stream().map(VerificationMethod::getId).toList());
    }

    @Test
    void getNonExistentParticipantDidDocument() {

        assertThrows(ParticipantNotFoundException.class, () -> sut.getParticipantDidDocument("non-existent"));
    }

    @Test
    void getParticipantDidDocumentGenerationFails() throws CertificateException {

        when(certificateService.convertPemStringToCertificate(any())).thenThrow(
            new CertificateException("bad certificate"));

        ParticipantDidDataEntity participantDidDataEntity = getTestParticipantCertificate();
        participantDidDataRepository.save(participantDidDataEntity);

        assertThrows(DidDocumentGenerationException.class,
            () -> sut.getParticipantDidDocument("c0334816-5608-387d-b935-7894158d4b1c"));

        reset(certificateService);
    }

    private ParticipantDidDataEntity getTestParticipantCertificate() {

        ParticipantDidDataEntity participantDidDataEntity = new ParticipantDidDataEntity();
        participantDidDataEntity.setDid("did:web:localhost%3A8443:participant:c0334816-5608-387d-b935-7894158d4b1c");
        participantDidDataEntity.setVerificationMethods(Collections.emptyList());
        participantDidDataEntity.setAliases(Collections.emptyList());
        return participantDidDataEntity;
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
