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

package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.utils.DidUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
public interface DidController {
    @Operation(summary = "Get a specific did document", tags = {
        "Public" }, description = "Get a specific did document for the given participant ID", parameters = {
        @Parameter(name = "participantId", description = "The participant ID for which to get the did document", example = "someorgltd") })
    @GetMapping(value = "/participant/{participantId}/"
        + DidUtils.DID_DOCUMENT_FILE, produces = MediaType.APPLICATION_JSON_VALUE)
    DidDocument getDidDocument(@PathVariable(value = "participantId") String participantId);

    @Operation(summary = "Get a specific certificate", tags = {
        "Public" }, description = "Get a specific certificate for the given participant ID and certificate ID", parameters = {
        @Parameter(name = "participantId", description = "The participant ID for which to get the did document", example = "someorgltd"),
        @Parameter(name = "certificateId", description = "The certificate ID specific to this participant", example = "someorgltd-example-cert") }, responses = {
        @ApiResponse(content = @Content(schema = @Schema(description = "X.509 certificate", example = "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"))) })
    @GetMapping(value = "/participant/{participantId}/{certificateId}.pem", produces = "application/x-x509-ca-cert")
    String getCertificate(@PathVariable(value = "participantId") String participantId,
        @PathVariable(value = "certificateId") String certificateId);

    @Operation(summary = "Get common dataspace did document", tags = {
        "Public" }, description = "Get the common dataspace did document")
    @GetMapping(value = "/.well-known/" + DidUtils.DID_DOCUMENT_FILE, produces = MediaType.APPLICATION_JSON_VALUE)
    DidDocument getCommonDidDocument();

    @Operation(summary = "Get common dataspace certificate", tags = {
        "Public" }, description = "Get the common dataspace certificate", responses = {
        @ApiResponse(content = @Content(schema = @Schema(description = "X.509 certificate", example = "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"))) })
    @GetMapping(value = "/.well-known/" + DidUtils.COMMON_CERTIFICATE_FILE, produces = "application/x-x509-ca-cert")
    String getCommonCertificate();
}
