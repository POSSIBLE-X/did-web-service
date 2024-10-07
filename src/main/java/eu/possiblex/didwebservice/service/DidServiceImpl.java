/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.possiblex.didwebservice.service;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.PublicJwk;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidData;
import eu.possiblex.didwebservice.models.exceptions.*;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Service
public class DidServiceImpl implements DidService {
    private static final String VM_TYPE_ID = "#JWK2020-PossibleLetsEncrypt";
    private static final String VM_TYPE = "JsonWebKey2020";
    private static final String VM_CONTEXT = "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/";

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final ObjectMapper objectMapper;

    private final String didDomain;

    private final String defaultCertPath;

    public DidServiceImpl(@Value("${common-cert-path:#{null}}") String defaultCertPath,
                          @Value("${did-domain}") String didDomain,
                          @Autowired ObjectMapper objectMapper,
                          @Autowired ParticipantDidDataRepository participantDidDataRepository
        ) {

        this.defaultCertPath = defaultCertPath;
        this.didDomain = didDomain;
        this.objectMapper = objectMapper;
        this.participantDidDataRepository = participantDidDataRepository;
    }

    @Override
    public String getCommonCertificate() throws CertificateException {
        return getCommonCertificatePemString();
    }

    @Override
    @Transactional
    public String getDidDocument(String id) throws ParticipantNotFoundException, DidDocumentGenerationException {

        String didWeb = getDidWebForParticipant(id);

        ParticipantDidData participantDidData = participantDidDataRepository.findByDid(didWeb);

        if (participantDidData == null) {
            throw new ParticipantNotFoundException("Participant could not be found.");
        }

        String didDocumentString = null;

        try {
            didDocumentString = createDidDocument(participantDidData);
        } catch (Exception e) {
            throw new DidDocumentGenerationException(e.getMessage());
        }

        return didDocumentString;
    }

    @Override
    public String getCommonDidDocument() throws DidDocumentGenerationException {
        try {
            ParticipantDidData federationCert = new ParticipantDidData();
            federationCert.setDid(getCommonDidWeb());
            return createDidDocument(federationCert);
        } catch (CertificateException | JsonProcessingException | PemConversionException e) {
            throw new DidDocumentGenerationException("Failed to build did.json for Federation: " + e.getMessage());
        }

    }

    /**
     * Generates a did:web. Returns the did:web, the verification method as
     * well as the associated private key and stores the certificate.
     * NOTE: currently MERLOT is no longer using the generated private/public key pair as it switched
     * to using the Federation Let's Encrypt certificate for signature. For future extensions this old key pair is
     * left in the code.
     *
     * TODO update description
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web, the verification method and the associated private key
     */
    public ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request)
        throws RequestArgumentException {

        String certificateSubject = request.getSubject();

        if (certificateSubject == null || certificateSubject.isBlank()) {
            throw new RequestArgumentException("Missing or empty subject name.");
        }

        String didWeb = generateDidWeb(certificateSubject);

        storeDidDocument(didWeb);
        return createParticipantDidPrivateKeyDto(didWeb);
    }

    private String generateDidWeb(String seed) {

        String uuid = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
        return getDidWebForParticipant(uuid);
    }

    private String getDidWebForParticipant(String id) {

        return getCommonDidWeb() + ":participant:" + id;
    }

    private String getCommonDidWeb() {

        return "did:web:" + didDomain.replaceFirst(":", "%3A");
    }

    private void storeDidDocument(String did) {

        ParticipantDidData cert = new ParticipantDidData();
        cert.setDid(did);

        participantDidDataRepository.save(cert);
    }

    private ParticipantDidTo createParticipantDidPrivateKeyDto(String did) {

        ParticipantDidTo dto = new ParticipantDidTo();
        dto.setDid(did);
        dto.setVerificationMethod(did + VM_TYPE_ID);

        return dto;
    }

    private String createDidDocument(ParticipantDidData participantDidData)
        throws JsonProcessingException, PemConversionException, CertificateException {

        String didWebParticipant = participantDidData.getDid();

        DidDocument didDocument = new DidDocument();
        didDocument.setContext(List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/jws-2020/v1"));
        didDocument.setId(didWebParticipant);
        didDocument.setVerificationMethod(new ArrayList<>());

        VerificationMethod commonVerificationMethod = getVerificationMethod(getCommonDidWeb(), getCommonCertificatePemString());
        commonVerificationMethod.setId(didWebParticipant + VM_TYPE_ID);
        didDocument.getVerificationMethod().add(commonVerificationMethod);

        // Return JSON string converted the DID object
        return objectMapper.writeValueAsString(didDocument);
    }

    private VerificationMethod getVerificationMethod(String didWeb, String certificate) throws PemConversionException {
        VerificationMethod vm = new VerificationMethod();
        vm.setContext(List.of(VM_CONTEXT));
        vm.setId(didWeb + VM_TYPE_ID);
        vm.setType(VM_TYPE);
        vm.setController(didWeb);

        X509Certificate x509Certificate = null;
        try {
            x509Certificate = convertPemStringToCertificate(certificate);
        } catch (CertificateException e) {
            throw new PemConversionException("Certificate conversion failed: " + e.getMessage());
        }

        RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();
        String e = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
        String n = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());

        PublicJwk publicKeyJwk = new PublicJwk();
        publicKeyJwk.setKty("RSA");
        publicKeyJwk.setN(n);
        publicKeyJwk.setE(e);
        publicKeyJwk.setAlg("PS256");

        String didWebBase = didWeb.replace("did:web:", "") // remove did type prefix
            .replaceFirst("#.*", ""); // remove verification method reference
        String certificateUrl = getDidDocumentUri(didWebBase).replace("did.json", "cert.ss.pem");

        publicKeyJwk.setX5u(certificateUrl);

        vm.setPublicKeyJwk(publicKeyJwk);

        return vm;
    }

    /**
     * Given the domain part of the did:web, return the resulting URI. See <a
     * href="https://w3c-ccg.github.io/did-method-web/#read-resolve">did-web specification</a> for reference.
     *
     * @param didWeb did:web without prefix and key reference
     * @return did web URI
     */
    private static String getDidDocumentUri(String didWeb) {

        boolean containsSubpath = didWeb.contains(":");
        StringBuilder didDocumentUriBuilder = new StringBuilder();
        didDocumentUriBuilder.append(
            didWeb.replace(":", "/") // Replace ":" with "/" in the method specific identifier to
                // obtain the fully qualified domain name and optional path.
                .replace("%3A", ":")); // If the domain contains a port percent decode the colon.

        // Generate an HTTPS URL to the expected location of the DID document by prepending https://.
        didDocumentUriBuilder.insert(0, "https://");
        if (!containsSubpath) {
            // If no path has been specified in the URL, append /.well-known.
            didDocumentUriBuilder.append("/.well-known");
        }
        // Append /did.json to complete the URL.
        didDocumentUriBuilder.append("/did.json");

        return didDocumentUriBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private X509Certificate convertPemStringToCertificate(String certs) throws CertificateException {

        ByteArrayInputStream certStream = new ByteArrayInputStream(certs.getBytes(StandardCharsets.UTF_8));

        List<X509Certificate> certificateList = null;

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        certificateList = (List<X509Certificate>) certFactory.generateCertificates(certStream);

        return certificateList.stream().findFirst().orElse(null);
    }

    /**
     * Load the common certificate from file.
     *
     * @return string representation of certificate
     * @throws CertificateException error during loading of the certificate
     */
    private String getCommonCertificatePemString() throws CertificateException {
        try (InputStream certificateStream = StringUtil.isNullOrEmpty(defaultCertPath) ?
            DidServiceImpl.class.getClassLoader().getResourceAsStream("cert.ss.pem")
            : new FileInputStream(defaultCertPath)) {
            return new String(Objects.requireNonNull(certificateStream,
                "Certificate input stream is null.").readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new CertificateException("Failed to read common certificate. " + e.getMessage());
        }
    }
}
