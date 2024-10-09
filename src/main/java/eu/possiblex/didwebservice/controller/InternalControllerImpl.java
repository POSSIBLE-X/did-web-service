package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.service.DidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class InternalControllerImpl implements InternalController {

    private final DidService didService;

    public InternalControllerImpl(@Autowired DidService didService) {
        this.didService = didService;
    }

    @Override
    public ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to) {
        try {
            return didService.generateParticipantDidWeb(to);
        } catch (RequestArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Did web generation failed: " + e.getMessage());
        }

    }
}
