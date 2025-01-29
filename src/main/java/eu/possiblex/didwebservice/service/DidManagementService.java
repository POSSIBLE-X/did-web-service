package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;

public interface DidManagementService {
    /**
     * Generates a did:web, a key pair and certificate. Returns the did:web and private key.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web
     */
    ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request);

    /**
     * Updates an existing did:web with new content.
     *
     * @param request updated information, null for info that should stay the same
     * @return dto containing the updated did:web
     */
    ParticipantDidTo updateParticipantDidWeb(ParticipantDidUpdateRequestTo request);

    /**
     * Removes an existing did:web if it exists.
     *
     * @param did did to remove
     */
    void removeParticipantDidWeb(String did);
}
