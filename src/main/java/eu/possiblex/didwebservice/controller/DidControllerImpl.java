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

import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.service.DidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.cert.CertificateException;

import static org.springframework.http.HttpStatus.*;

@RestController

public class DidControllerImpl implements DidController {
    private final DidService didService;

    public DidControllerImpl(@Autowired DidService didService) {
        this.didService = didService;
    }

    /**
     * GET endpoint for retrieving the DID document for given participant.
     *
     * @param id id for retrieving the DID document
     * @return participant DID document
     */
    @Override
    public ResponseEntity<String> getDidDocument(@PathVariable(value = "id") String id) {

        String didDocument;
        try {
            didDocument = didService.getDidDocument(id);
        } catch (ParticipantNotFoundException e1) {
            throw new ResponseStatusException(NOT_FOUND, e1.getMessage());
        } catch (DidDocumentGenerationException e2) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
                "Did document provision failed: " + e2.getMessage());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(didDocument, httpHeaders, HttpStatus.OK);
    }

    /**
     * GET endpoint for retrieving the DID document for the federation.
     *
     * @return Common DID document
     */
    @Override
    public String getCommonDidDocument() {
        try {
            return didService.getCommonDidDocument();
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
    public String getCertificate() {
        try {
            return didService.getCommonCertificate();
        } catch (CertificateException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to load federation certificate.");
        }

    }
}
