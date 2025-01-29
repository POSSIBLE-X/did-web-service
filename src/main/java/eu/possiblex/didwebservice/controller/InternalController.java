package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/internal")
public interface InternalController {
    @Operation(summary = "Generate a new did:web identity", tags = {
        "Management" }, description = "Generate a new did:web identity based on the data in the payload")
    @PostMapping("/didweb")
    ParticipantDidTo generateDidWeb(@RequestBody ParticipantDidCreateRequestTo to);

    @Operation(summary = "Update an existing did:web identity", tags = {
        "Management" }, description = "Update a did:web identity that is managed by this did-web-service. Fields with null value are ignored.")
    @PatchMapping("/didweb")
    void updateDidWeb(@RequestBody ParticipantDidUpdateRequestTo to);

    @Operation(summary = "Delete a did:web identity", tags = {
        "Management" }, description = "Delete the provided did:web identity that is managed by this did-web-service")
    @DeleteMapping("/didweb/{did}")
    void removeDidWeb(@PathVariable String did);
}
