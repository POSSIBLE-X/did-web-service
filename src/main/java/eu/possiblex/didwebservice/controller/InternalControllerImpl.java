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
