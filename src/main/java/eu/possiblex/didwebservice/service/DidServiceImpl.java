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
import eu.possiblex.didwebservice.models.dto.ParticipantDidRemoveRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidData;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.PemConversionException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Service
@Slf4j
public class DidServiceImpl implements DidService {
    private static final String VM_TYPE_ID = "#JWK2020-PossibleLetsEncrypt";

    private static final String VM_TYPE = "JsonWebKey2020";

    private static final String VM_CONTEXT = "https://w3c-ccg.github.io/lds-jws2020/contexts/v1/";

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final ObjectMapper objectMapper;

    private final String didDomain;

    private final String defaultCertPath;

    public DidServiceImpl(@Value("${common-cert-path:#{null}}") String defaultCertPath,
        @Value("${did-domain}") String didDomain, @Autowired ObjectMapper objectMapper,
        @Autowired ParticipantDidDataRepository participantDidDataRepository) {

        this.defaultCertPath = defaultCertPath;
        this.didDomain = didDomain;
        this.objectMapper = objectMapper;
        this.participantDidDataRepository = participantDidDataRepository;
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

    /**
     * Get the content of the federation-wide common certificate in PEM format.
     *
     * @return common certificate
     * @throws CertificateException error during reading the certificate from disk
     */
    @Override
    public String getCommonCertificate() throws CertificateException {

        return getCommonCertificatePemString();
    }

    /**
     * Get the DID document for a given participant DID.
     *
     * @param id id to retrieve the DID document for
     * @return did document
     * @throws ParticipantNotFoundException participant with given did did not exist in db
     * @throws DidDocumentGenerationException failed to build did document from database data
     */
    @Override
    @Transactional
    public String getDidDocument(String id) throws ParticipantNotFoundException, DidDocumentGenerationException {

        String didWeb = getDidWebForParticipant(id);

        ParticipantDidData participantDidData = participantDidDataRepository.findByDid(didWeb);

        if (participantDidData == null) {
            throw new ParticipantNotFoundException("Participant could not be found.");
        }

        try {
            return createDidDocument(participantDidData);
        } catch (Exception e) {
            throw new DidDocumentGenerationException(e.getMessage());
        }
    }

    /**
     * Get the common DID document for the federation.
     *
     * @return federation did document
     * @throws DidDocumentGenerationException failed to build did document from database data
     */
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
     * Generates a did:web. Returns the did:web and the verification method.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web, the verification method and the associated private key
     */
    @Transactional
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

    /**
     * Updates an existing did:web with new content.
     *
     * @param request updated information, null for info that should stay the same
     * @throws RequestArgumentException invalid request
     */
    @Override
    @Transactional
    public void updateParticipantDidWeb(ParticipantDidUpdateRequestTo request)
        throws RequestArgumentException, ParticipantNotFoundException {

        String didWeb = request.getDid();

        if (didWeb == null || didWeb.isBlank()) {
            throw new RequestArgumentException("Missing or empty did.");
        }

        ParticipantDidData participantDidData = participantDidDataRepository.findByDid(didWeb);

        if (participantDidData == null) {
            throw new ParticipantNotFoundException("Did does not exist in the database.");
        }

        if (request.getAliases() != null) {
            participantDidData.setAliases(request.getAliases());
        }
    }

    /**
     * Removes an existing did:web if it exists.
     *
     * @param request with information needed for removal
     * @throws RequestArgumentException did parameter not specified
     */
    @Transactional
    @Override
    public void removeParticipantDidWeb(ParticipantDidRemoveRequestTo request) throws RequestArgumentException {

        String didWeb = request.getDid();

        if (didWeb == null || didWeb.isBlank()) {
            throw new RequestArgumentException("Missing or empty did.");
        }

        deleteDidDocument(didWeb);
    }

    /**
     * generate a did-web identifier based on the given seed.
     *
     * @param seed seed to generate the did-web from
     * @return generated did-web identifier
     */
    private String generateDidWeb(String seed) {

        String uuid = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
        return getDidWebForParticipant(uuid);
    }

    /**
     * Build the full did-web path for a given participant id.
     *
     * @param id participant id
     * @return did-web path
     */
    private String getDidWebForParticipant(String id) {

        return getCommonDidWeb() + ":participant:" + id;
    }

    /**
     * Get the did-web base path for the currently configured domain.
     *
     * @return did-web base path
     */
    private String getCommonDidWeb() {

        return "did:web:" + didDomain.replaceFirst(":", "%3A");
    }

    /**
     * Create a new database entry for the given did if it does not already exist. If it already exists, log it and do
     * nothing.
     *
     * @param did did to store in the database
     */
    private void storeDidDocument(String did) {

        if (participantDidDataRepository.findByDid(did) != null) {
            log.info("Did {} already exists in the database.", did);
            return;
        }
        ParticipantDidData data = new ParticipantDidData();
        data.setDid(did);

        participantDidDataRepository.save(data);
    }

    private void deleteDidDocument(String did) {

        if (participantDidDataRepository.findByDid(did) == null) {
            log.info("Did {} does not exist in the database.", did);
            return;
        }
        participantDidDataRepository.deleteByDid(did);
    }

    /**
     * Generate a DTO containing the did and the verification method for a given did.
     *
     * @param did did to generate the DTO for
     * @return DTO containing the did and the verification method
     */
    private ParticipantDidTo createParticipantDidPrivateKeyDto(String did) {

        ParticipantDidTo dto = new ParticipantDidTo();
        dto.setDid(did);
        dto.setVerificationMethod(did + VM_TYPE_ID);

        return dto;
    }

    /**
     * Given a database entry for a participant, build the corresponding did document.
     *
     * @param participantDidData participant data to build the did document for
     * @return JSON string representation of the did document
     * @throws JsonProcessingException failed to convert the did document to JSON
     * @throws PemConversionException failed to convert the certificate to a public key
     * @throws CertificateException failed to load the common certificate
     */
    private String createDidDocument(ParticipantDidData participantDidData)
        throws JsonProcessingException, PemConversionException, CertificateException {

        // get did identifier from db data
        String didWebParticipant = participantDidData.getDid();

        // build did document
        DidDocument didDocument = new DidDocument();
        didDocument.setContext(List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/jws-2020/v1"));
        didDocument.setId(didWebParticipant);
        didDocument.setVerificationMethod(new ArrayList<>());
        didDocument.setAlsoKnownAs(participantDidData.getAliases());

        // add common federation verification method
        VerificationMethod commonVerificationMethod = getVerificationMethod(getCommonDidWeb(),
            getCommonCertificatePemString());
        commonVerificationMethod.setId(didWebParticipant + VM_TYPE_ID);
        didDocument.getVerificationMethod().add(commonVerificationMethod);

        // Return JSON string converted the DID object
        return objectMapper.writeValueAsString(didDocument);
    }

    /**
     * Build the verification method for a given did identifier and string representation of a certificate.
     *
     * @param didWeb did-web identifier
     * @param certificate certificate in PEM format
     * @return verification method
     * @throws PemConversionException failed to convert the certificate to a public key
     */
    private VerificationMethod getVerificationMethod(String didWeb, String certificate) throws PemConversionException {

        // setup verification method
        VerificationMethod vm = new VerificationMethod();
        vm.setContext(List.of(VM_CONTEXT));
        vm.setId(didWeb + VM_TYPE_ID);
        vm.setType(VM_TYPE);
        vm.setController(didWeb);

        // load certificate from string
        X509Certificate x509Certificate;
        try {
            x509Certificate = convertPemStringToCertificate(certificate);
        } catch (CertificateException e) {
            throw new PemConversionException("Certificate conversion failed: " + e.getMessage());
        }

        // load public key from certificate and build parameters for JWK
        RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();
        String e = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
        String n = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());

        // build JWK for VM
        PublicJwk publicKeyJwk = new PublicJwk();
        publicKeyJwk.setKty("RSA");
        publicKeyJwk.setN(n);
        publicKeyJwk.setE(e);
        publicKeyJwk.setAlg("PS256");

        // build url under which the certificate will be hosted
        String didWebBase = didWeb.replace("did:web:", "") // remove did type prefix
            .replaceFirst("#.*", ""); // remove verification method reference
        String certificateUrl = getDidDocumentUri(didWebBase).replace("did.json", "cert.ss.pem");

        // set url reference to certificate in JWK
        publicKeyJwk.setX5u(certificateUrl);

        // set JWK in VM
        vm.setPublicKeyJwk(publicKeyJwk);

        return vm;
    }

    /**
     * Convert a PEM string to a X509 certificate object.
     *
     * @param certs PEM string representation of the certificate
     * @return X509 certificate object
     * @throws CertificateException error during conversion of the certificate
     */
    @SuppressWarnings("unchecked")
    private X509Certificate convertPemStringToCertificate(String certs) throws CertificateException {

        ByteArrayInputStream certStream = new ByteArrayInputStream(certs.getBytes(StandardCharsets.UTF_8));

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        return ((List<X509Certificate>) certFactory.generateCertificates(certStream)).stream().findFirst().orElse(null);
    }

    /**
     * Load the common certificate from file.
     *
     * @return string representation of certificate
     * @throws CertificateException error during loading of the certificate
     */
    private String getCommonCertificatePemString() throws CertificateException {

        try (InputStream certificateStream = StringUtil.isNullOrEmpty(defaultCertPath)
            ? DidServiceImpl.class.getClassLoader().getResourceAsStream("cert.ss.pem")
            : new FileInputStream(defaultCertPath)) {
            return new String(
                Objects.requireNonNull(certificateStream, "Certificate input stream is null.").readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new CertificateException("Failed to read common certificate. " + e.getMessage());
        }
    }
}
