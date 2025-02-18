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

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/internal")
public interface InternalController {
    @Operation(summary = "Generate a new did:web identity", tags = {
        "Management" }, description = "Generate a new did:web identity based on the data in the payload. Only the subject is mandatory.")
    @PostMapping(value = "/didweb", produces = MediaType.APPLICATION_JSON_VALUE)
    ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to);

    @Operation(summary = "Update an existing did:web identity", tags = {
        "Management" }, description = "Update a did:web identity that is managed by this did-web-service. Fields with null value are ignored.")
    @PatchMapping(value = "/didweb", produces = MediaType.APPLICATION_JSON_VALUE)
    ParticipantDidTo updateDidWeb(@RequestBody ParticipantDidUpdateRequestTo to);

    @Operation(summary = "Delete a did:web identity", tags = {
        "Management" }, description = "Delete the provided did:web identity that is managed by this did-web-service", parameters = {
        @Parameter(name = "did", description = "The did:web managed by this Service to be deleted", example = "did:web:example.com:participant:someorgltd") })
    @DeleteMapping("/didweb/{did}")
    void removeDidWeb(@PathVariable String did);
}
