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

package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.service.CertificateService;
import eu.possiblex.didwebservice.service.DidDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController

public class DidControllerImpl implements DidController {
    private final DidDocumentService didDocumentService;

    private final CertificateService certificateService;

    public DidControllerImpl(@Autowired DidDocumentService didDocumentService,
        @Autowired CertificateService certificateService) {

        this.didDocumentService = didDocumentService;
        this.certificateService = certificateService;
    }

    /**
     * GET endpoint for retrieving the DID document for given participant.
     *
     * @param participantId id for retrieving the DID document
     * @return participant DID document
     */
    @Override
    public DidDocument getDidDocument(@PathVariable(value = "participantId") String participantId) {

        try {
            return didDocumentService.getParticipantDidDocument(participantId);
        } catch (ParticipantNotFoundException e1) {
            throw new ResponseStatusException(NOT_FOUND, e1.getMessage());
        } catch (DidDocumentGenerationException e2) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
                "Did document provision failed: " + e2.getMessage());
        }
    }

    /**
     * GET endpoint for retrieving a particular certificate for a participant.
     *
     * @return specific participant certificate
     */
    @Override
    public String getCertificate(@PathVariable(value = "participantId") String participantId,
        @PathVariable(value = "certificateId") String certificateId) {

        try {
            return certificateService.getParticipantCertificate(participantId, certificateId);
        } catch (ParticipantNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET endpoint for retrieving the DID document for the federation.
     *
     * @return Common DID document
     */
    @Override
    public DidDocument getCommonDidDocument() {

        try {
            return didDocumentService.getCommonDidDocument();
        } catch (DidDocumentGenerationException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
                "Did document provision failed: " + e.getMessage());
        }
    }

    /**
     * GET endpoint for retrieving the certificate for the federation.
     *
     * @return Common certificate
     */
    @Override
    public String getCommonCertificate() {

        return certificateService.getCommonCertificate();
    }
}
