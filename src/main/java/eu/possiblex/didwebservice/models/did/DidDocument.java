/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *  Copyright 2024-2025 Dataport. All rights reserved. Extended as part of the POSSIBLE project.
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

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@JsonPropertyOrder({ "@context", "id", "verificationMethod" })
public class DidDocument {
    @JsonProperty("@context")
    @Schema(description = "JSON-LD context", example = "[\"https://www.w3.org/ns/did/v1\", \"https://w3id.org/security/suites/jws-2020/v1\"]")
    @Builder.Default
    private List<String> context = List.of("https://www.w3.org/ns/did/v1",
        "https://w3id.org/security/suites/jws-2020/v1");

    @Schema(description = "did:web identity", example = "did:web:example.com:participant:someorgltd")
    private String id;

    @Schema(description = "List of verification methods associated with this identity")
    private List<VerificationMethod> verificationMethod = new ArrayList<>();

    @Schema(description = "List of aliases associated with this identity", example = "[\"https://someorganization.com\"]")
    private List<String> alsoKnownAs;
}
