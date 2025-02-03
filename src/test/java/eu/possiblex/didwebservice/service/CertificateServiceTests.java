package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.DidWebServiceApplication;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = { CertificateServiceTests.TestConfig.class, CertificateServiceImpl.class,
    DidWebServiceApplication.class })
class CertificateServiceTests {
    @Autowired
    private CertificateService sut;

    @SpyBean
    private ParticipantDidDataRepository participantDidDataRepository;

    @Test
    void getCommonCertificate() {

        String commonCert = sut.getCommonCertificate();
        assertNotNull(commonCert);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DidWebUrlService didWebUrlService() {

            return Mockito.spy(new DidWebUrlServiceFake("localhost:8443"));
        }
    }
}
