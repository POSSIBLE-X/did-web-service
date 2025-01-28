package eu.possiblex.didwebservice.controller;

import eu.possiblex.didwebservice.models.did.DidDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
public interface DidController {
    @GetMapping(value = "/participant/{participantId}/did.json", produces = "application/json")
    DidDocument getDidDocument(@PathVariable(value = "participantId") String participantId);

    @GetMapping(value = "/participant/{participantId}/{certId}.pem", produces = "application/x-x509-ca-cert")
    String getCertificate(@PathVariable(value = "participantId") String participantId,
        @PathVariable(value = "certId") String certId);

    @GetMapping(value = "/.well-known/did.json", produces = "application/json")
    DidDocument getCommonDidDocument();

    @GetMapping(value = "/.well-known/cert.ss.pem", produces = "application/x-x509-ca-cert")
    String getCommonCertificate();
}
