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
import eu.possiblex.didwebservice.service.DidManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalControllerImpl implements InternalController {

    private final DidManagementService didManagementService;

    public InternalControllerImpl(@Autowired DidManagementService didManagementService) {

        this.didManagementService = didManagementService;
    }

    @Override
    public ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to) {

        return didManagementService.generateParticipantDidWeb(to);
    }

    @Override
    public ParticipantDidTo updateDidWeb(ParticipantDidUpdateRequestTo to) {

        return didManagementService.updateParticipantDidWeb(to);
    }

    @Override
    public void removeDidWeb(@PathVariable String did) {

        didManagementService.removeParticipantDidWeb(did);
    }
}
