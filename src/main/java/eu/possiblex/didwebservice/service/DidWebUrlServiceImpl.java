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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DidWebUrlServiceImpl implements DidWebUrlService {

    private final String didWebDomain;

    public DidWebUrlServiceImpl(@Value("${did-web-domain}") String didWebDomain) {

        this.didWebDomain = didWebDomain;
    }

    /**
     * Build the full did-web path for a given participant id.
     *
     * @param id participant id
     * @return did-web path
     */
    @Override
    public String getDidWebForParticipant(String id) {

        return getCommonDidWeb() + ":participant:" + id;
    }

    /**
     * Get the did-web base path for the currently configured domain.
     *
     * @return did-web base path
     */
    @Override
    public String getCommonDidWeb() {

        return "did:web:" + didWebDomain.replaceFirst(":", "%3A");
    }
}
