package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
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

        return ParticipantDidTo.builder().did(request.getDid())
            .verificationMethodIds(List.of(request.getDid() + "#somemethod")).aliases(request.getAliases()).build();
    }

    @Override
    public void removeParticipantDidWeb(String did) {
        // do nothing
    }
}
