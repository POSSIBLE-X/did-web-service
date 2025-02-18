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
