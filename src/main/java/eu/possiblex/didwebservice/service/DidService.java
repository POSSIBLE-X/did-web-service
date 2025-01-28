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

package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;

public interface DidService {

    /**
     * Returns a specific certificate for a participant.
     *
     * @param participantId id of the participant
     * @param certId id of the certificate
     * @return the certificate
     */
    String getParticipantCertificate(String participantId, String certId);

    /**
     * Returns a certificate for the federation.
     *
     * @return the certificate
     */
    String getCommonCertificate();

    /**
     * Returns the DID document for a given id.
     *
     * @param id id to retrieve the DID document with
     * @return the did document as string
     */
    DidDocument getParticipantDidDocument(String id);

    /**
     * Returns the DID document for the dataspace federation.
     *
     * @return the did document
     */
    DidDocument getCommonDidDocument();

    /**
     * Generates a did:web, a key pair and certificate. Returns the did:web and private key.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web and private key
     */
    ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request);

    /**
     * Updates an existing did:web with new content.
     *
     * @param request updated information, null for info that should stay the same
     */
    void updateParticipantDidWeb(ParticipantDidUpdateRequestTo request);

    /**
     * Removes an existing did:web if it exists.
     *
     * @param did did to remove
     */
    void removeParticipantDidWeb(String did);
}
