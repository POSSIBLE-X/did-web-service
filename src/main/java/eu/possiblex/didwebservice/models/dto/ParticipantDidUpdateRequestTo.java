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

package eu.possiblex.didwebservice.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDidUpdateRequestTo {
    @NotBlank
    @Schema(description = "did:web identity that is enrolled in this did-web-service and which should be updated.", example = "did:web:example.com:participant:someorgltd")
    private String did;

    @Schema(description = "Optional list of URIs that also reference this identity. Ignored if null.", example = "[\"https://someorganization.com\"]")
    private List<String> aliases;

    @Schema(description = "Optional map of certificate ids and their contents that should be listed as verification methods in the did document. Ignored if null.", example = "{\"someorgltd-example-cert\": \"-----BEGIN CERTIFICATE-----\\n..\\n.-----END CERTIFICATE-----\"}")
    private Map<String, String> certificates;
}
