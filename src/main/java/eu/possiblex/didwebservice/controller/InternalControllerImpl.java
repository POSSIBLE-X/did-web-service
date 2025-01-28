package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.service.DidManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class InternalControllerImpl implements InternalController {

    private final DidManagementService didManagementService;

    public InternalControllerImpl(@Autowired DidManagementService didManagementService) {

        this.didManagementService = didManagementService;
    }

    @Override
    public ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to) {

        try {
            return didManagementService.generateParticipantDidWeb(to);
        } catch (RequestArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Did web generation failed: " + e.getMessage());
        }

    }

    @Override
    public void updateDidWeb(ParticipantDidUpdateRequestTo to) {

        try {
            didManagementService.updateParticipantDidWeb(to);
        } catch (RequestArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Did document update failed: " + e.getMessage());
        } catch (ParticipantNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public void removeDidWeb(@PathVariable String did) {

        try {
            didManagementService.removeParticipantDidWeb(did);
        } catch (RequestArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Did web removal failed: " + e.getMessage());
        }
    }
}
