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

package eu.possiblex.didwebservice.models.did;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@JsonPropertyOrder({ "@context", "id", "type", "controller", "publicKeyJwk" })
public class VerificationMethod {
    @JsonProperty("@context")
    @Schema(description = "JSON-LD context", example = "[\"https://w3c-ccg.github.io/lds-jws2020/contexts/v1/\"]")
    @Builder.Default
    private List<String> context = List.of("https://w3c-ccg.github.io/lds-jws2020/contexts/v1/");

    @Schema(description = "Verification method type", example = "JsonWebKey2020")
    @Builder.Default
    private String type = "JsonWebKey2020";

    @Schema(description = "Verification method ID", example = "did:web:example.com:participant:someorgltd#someorgltd-example-cert")
    private String id;

    @Schema(description = "Verification method controller", example = "did:web:example.com:participant:someorgltd")
    private String controller;

    @Schema(description = "Public JSON Web Key")
    private PublicJwk publicKeyJwk;
}
