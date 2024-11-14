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
import eu.possiblex.didwebservice.models.exceptions.*;

import java.security.cert.CertificateException;

public interface DidService {

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
    DidDocument getDidDocument(String id) throws ParticipantNotFoundException, DidDocumentGenerationException;

    /**
     * Returns the DID document for the MERLOT federation.
     *
     * @return the did document as string
     */
    DidDocument getCommonDidDocument() throws DidDocumentGenerationException;

    /**
     * Generates a did:web, a key pair and certificate. Returns the did:web and private key.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web and private key
     */
    ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request)
        throws RequestArgumentException;
}
