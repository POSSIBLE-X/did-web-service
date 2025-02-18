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

package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;

import java.util.List;

public class DidManagementServiceFake implements DidManagementService {
    @Override
    public ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request) {

        if (request.getSubject() == null) {
            throw new RequestArgumentException("empty subject");
        }

        return ParticipantDidTo.builder().did("did:web:example.com:participant:someorgltd")
            .verificationMethodIds(List.of("did:web:example.com:participant:someorgltd#somemethod"))
            .aliases(request.getAliases()).build();
    }

    @Override
    public ParticipantDidTo updateParticipantDidWeb(ParticipantDidUpdateRequestTo request) {

        if (request.getDid() == null) {
            throw new RequestArgumentException("empty did");
        }

        if (request.getDid().equals("did:web:example.com:participant:unknown")) {
            throw new ParticipantNotFoundException("did not found");
        }

        return ParticipantDidTo.builder().did(request.getDid())
            .verificationMethodIds(List.of(request.getDid() + "#somemethod")).aliases(request.getAliases()).build();
    }

    @Override
    public void removeParticipantDidWeb(String did) {
        // do nothing
    }
}
