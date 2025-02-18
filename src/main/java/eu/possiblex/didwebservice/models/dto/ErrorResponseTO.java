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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseTO {
    private OffsetDateTime timestamp;

    private String message;

    private String details;

    public ErrorResponseTO(String message) {

        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.details = "";
    }

    public ErrorResponseTO(String message, String details) {

        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.details = details;
    }
}
