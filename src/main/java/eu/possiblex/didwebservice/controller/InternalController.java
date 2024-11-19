package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidRemoveRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal")
public interface InternalController {
    @PostMapping("/didweb/generate")
    ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to);

    @DeleteMapping("/didweb/remove")
    void removeDidWeb(@RequestBody ParticipantDidRemoveRequestTo to);
}
