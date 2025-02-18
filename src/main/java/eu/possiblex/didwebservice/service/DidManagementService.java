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

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;

public interface DidManagementService {
    /**
     * Generates a did:web, a key pair and certificate. Returns the did:web and private key.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web
     */
    ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request);

    /**
     * Updates an existing did:web with new content.
     *
     * @param request updated information, null for info that should stay the same
     * @return dto containing the updated did:web
     */
    ParticipantDidTo updateParticipantDidWeb(ParticipantDidUpdateRequestTo request);

    /**
     * Removes an existing did:web if it exists.
     *
     * @param did did to remove
     */
    void removeParticipantDidWeb(String did);
}
