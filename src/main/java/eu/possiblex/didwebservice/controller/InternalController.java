package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidRemoveRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/internal")
public interface InternalController {
    @PostMapping("/didweb/generate")
    ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to);

    @PatchMapping("/didweb/update")
    void updateDidWeb(@RequestBody ParticipantDidUpdateRequestTo to);

    @DeleteMapping("/didweb/remove")
    void removeDidWeb(@RequestBody ParticipantDidRemoveRequestTo to);
}
