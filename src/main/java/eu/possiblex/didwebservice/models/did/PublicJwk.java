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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@JsonPropertyOrder({ "kty", "n", "e", "alg", "x5u" })
public class PublicJwk {
    @Schema(description = "The family of cryptographic algorithms used with the key.", example = "RSA")
    private String kty;

    @Schema(description = "The modulus for the RSA public key", example = "12345")
    private String n;

    @Schema(description = "The exponent for the RSA public key", example = "AQAB")
    private String e;

    @Schema(description = "The specific cryptographic algorithm used with the key.", example = "RS256")
    private String alg;

    @Schema(description = "The URL to the X.509 certificate chain", example = "https://example.com/cert.pem")
    private String x5u;
}
