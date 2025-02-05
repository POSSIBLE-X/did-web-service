package eu.possiblex.didwebservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = { DidWebUrlServiceTests.TestConfig.class, DidWebUrlServiceImpl.class })
class DidWebUrlServiceTests {

    @Autowired
    private DidWebUrlService sut;

    @Test
    void getCommonDidWebCorrectly() {

        String commonDid = sut.getCommonDidWeb();
        assertNotNull(commonDid);
        assertTrue(commonDid.startsWith("did:web:"));
    }

    @Test
    void getParticipantDidWebCorrectly() {

        String participantDid = sut.getDidWebForParticipant("123");
        assertNotNull(participantDid);
        assertTrue(participantDid.startsWith("did:web:"));
        assertTrue(participantDid.contains("participant:123"));
    }

    @TestConfiguration
    static class TestConfig {
    }
}
