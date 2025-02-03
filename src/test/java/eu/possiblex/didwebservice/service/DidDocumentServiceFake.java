package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.PublicJwk;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;

import java.util.List;

public class DidDocumentServiceFake implements DidDocumentService {
    @Override
    public DidDocument getParticipantDidDocument(String id) {

        if (id.equals("unknown-participant")) {
            throw new ParticipantNotFoundException("not found");
        }

        if (id.equals("broken-certificate")) {
            throw new DidDocumentGenerationException("not found");
        }

        return DidDocument.builder().id(id).verificationMethod(List.of(
            VerificationMethod.builder().id(id + "#somemethod").controller(id).publicKeyJwk(
                PublicJwk.builder().alg("RS256").e("AQAB").kty("RSA").x5u("https://example.com/cert.pem").n("12345")
                    .build()).build())).build();
    }

    @Override
    public DidDocument getCommonDidDocument() {

        return DidDocument.builder().id("did:web:example.com").verificationMethod(List.of(
            VerificationMethod.builder().id("did:web:example.com#somemethod").controller("did:web:example.com")
                .publicKeyJwk(
                    PublicJwk.builder().alg("RS256").e("AQAB").kty("RSA").x5u("https://example.com/cert.pem").n("12345")
                        .build()).build())).build();
    }
}
