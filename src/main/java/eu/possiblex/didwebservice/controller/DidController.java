package eu.possiblex.didwebservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
public interface DidController {
    @GetMapping(value = "/participant/{id}/did.json", produces = "application/json")
    ResponseEntity<String> getDidDocument(@PathVariable(value = "id") String id);

    @GetMapping(value = "/.well-known/did.json", produces = "application/json")
    String getCommonDidDocument();

    @GetMapping(value = "/.well-known/cert.ss.pem", produces = "application/x-x509-ca-cert")
    String getCertificate();
}
