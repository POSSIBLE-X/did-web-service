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
import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.PublicJwk;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
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

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final String serviceDomain;

    private final String commonCertificateString;

    public DidServiceImpl(@Value("${common-cert-path:#{null}}") String defaultCertPath,
        @Value("${did-domain}") String serviceDomain,
        @Autowired ParticipantDidDataRepository participantDidDataRepository) throws CertificateException {

        this.serviceDomain = serviceDomain;
        this.participantDidDataRepository = participantDidDataRepository;
        this.commonCertificateString = getCommonCertificatePemString(defaultCertPath);
    }

    /**
     * Given a valid did:web, return the resulting URI to the did document. See <a
     * href="https://w3c-ccg.github.io/did-method-web/#read-resolve">did-web specification</a> for reference.
     *
     * @param didWeb did:web
     * @return did web URI
     */
    private static String getDidDocumentUri(String didWeb) {

        boolean containsSubpath = didWeb.contains(":");
        StringBuilder didDocumentUriBuilder = new StringBuilder();
        didDocumentUriBuilder.append(didWeb.replace("did:web:", "") // remove prefix
            .replace(":", "/") // Replace ":" with "/" in the method specific identifier to
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
     * Get the content of a specific certificate for a participant in PEM format.
     *
     * @param participantId id of the participant
     * @param certId id of the certificate
     * @return certificate
     */
    public String getParticipantCertificate(String participantId, String certId) throws ParticipantNotFoundException {

        String didWeb = getDidWebForParticipant(participantId);

        ParticipantDidDataEntity participantDidDataEntity = participantDidDataRepository.findByDid(didWeb);

        if (participantDidDataEntity == null) {
            throw new ParticipantNotFoundException("Participant could not be found.");
        }

        return participantDidDataEntity.getVerificationMethods().stream()
            .filter(vm -> vm.getCertificateId().equals(certId)).map(VerificationMethodEntity::getCertificate)
            .findFirst().orElse(null);
    }

    /**
     * Get the content of the federation-wide common certificate in PEM format.
     *
     * @return common certificate
     */
    @Override
    public String getCommonCertificate() {

        return commonCertificateString;
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
    public DidDocument getParticipantDidDocument(String id)
        throws ParticipantNotFoundException, DidDocumentGenerationException {

        String didWeb = getDidWebForParticipant(id);

        ParticipantDidDataEntity participantDidDataEntity = participantDidDataRepository.findByDid(didWeb);

        if (participantDidDataEntity == null) {
            throw new ParticipantNotFoundException("Participant could not be found.");
        }

        try {
            return createDidDocument(participantDidDataEntity);
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
    public DidDocument getCommonDidDocument() throws DidDocumentGenerationException {

        try {
            ParticipantDidDataEntity federationCert = new ParticipantDidDataEntity();
            federationCert.setDid(getCommonDidWeb());
            return createDidDocument(federationCert);
        } catch (CertificateException | JsonProcessingException | PemConversionException e) {
            throw new DidDocumentGenerationException("Failed to build did.json for Federation: " + e.getMessage());
        }

    }

    /**
     * Generates a did:web entry. Returns the did:web and the verification methods.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web and the verification methods
     */
    @Transactional
    public ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request)
        throws RequestArgumentException {

        String certificateSubject = request.getSubject();

        if (certificateSubject == null || certificateSubject.isBlank()) {
            throw new RequestArgumentException("Missing or empty subject name.");
        }

        String didWeb = generateDidWeb(certificateSubject);

        ParticipantDidDataEntity entity = storeDidDocument(didWeb, request.getCertificates());
        List<String> verificationMethodIds = new ArrayList<>();
        verificationMethodIds.add(entity.getDid() + "#JWK2020-PossibleLetsEncrypt");
        verificationMethodIds.addAll(
            entity.getVerificationMethods().stream().map(vm -> entity.getDid() + "#" + vm.getCertificateId()).toList());

        return new ParticipantDidTo(entity.getDid(), verificationMethodIds);
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

        return "did:web:" + serviceDomain.replaceFirst(":", "%3A");
    }

    /**
     * Create a new database entry for the given did if it does not already exist. If it already exists, log it and do
     * nothing.
     *
     * @param did did to store in the database
     */
    private ParticipantDidDataEntity storeDidDocument(String did, Map<String, String> certificates)
        throws RequestArgumentException {

        ParticipantDidDataEntity data = participantDidDataRepository.findByDid(did);
        if (data != null) {
            log.info("Did {} already exists in the database.", did);
            return data;
        }
        data = new ParticipantDidDataEntity();
        data.setDid(did);

        List<VerificationMethodEntity> verificationMethods = getVerificationMethodEntities(certificates);

        data.setVerificationMethods(verificationMethods);

        return participantDidDataRepository.save(data);
    }

    private List<VerificationMethodEntity> getVerificationMethodEntities(Map<String, String> certificates)
        throws RequestArgumentException {

        if (certificates == null) {
            return Collections.emptyList();
        }

        List<VerificationMethodEntity> verificationMethods = new ArrayList<>();
        for (var certEntry : certificates.entrySet()) {
            try {
                convertPemStringToCertificate(certEntry.getValue());
            } catch (CertificateException e) {
                throw new RequestArgumentException("Certificate with ID " + certEntry.getKey() + " is not valid.");
            }

            if (!certEntry.getKey().matches("^[0-9A-Za-z-]+$")) {
                throw new RequestArgumentException("Certificate has invalid characters in ID: " + certEntry.getKey());
            }

            VerificationMethodEntity verificationMethodEntity = new VerificationMethodEntity();
            verificationMethodEntity.setCertificateId(certEntry.getKey());
            verificationMethodEntity.setCertificate(certEntry.getValue());
            verificationMethods.add(verificationMethodEntity);
        }
        return verificationMethods;
    }

    /**
     * Given a database entry for a participant, build the corresponding did document.
     *
     * @param participantDidDataEntity participant data to build the did document for
     * @return JSON string representation of the did document
     * @throws JsonProcessingException failed to convert the did document to JSON
     * @throws PemConversionException failed to convert the certificate to a public key
     * @throws CertificateException failed to load the common certificate
     */
    private DidDocument createDidDocument(ParticipantDidDataEntity participantDidDataEntity)
        throws JsonProcessingException, PemConversionException, CertificateException {

        // get did identifier from db data
        String didWebParticipant = participantDidDataEntity.getDid();

        // build did document
        DidDocument didDocument = new DidDocument();
        didDocument.setId(didWebParticipant);

        for (VerificationMethodEntity vmEntity : participantDidDataEntity.getVerificationMethods()) {
            String certificateUrl = getDidDocumentUri(didWebParticipant).replace("did.json",
                vmEntity.getCertificateId() + ".ss.pem");
            String verificationMethodId = didWebParticipant + "#" + vmEntity.getCertificateId();
            didDocument.getVerificationMethod().add(
                getVerificationMethod(didWebParticipant, verificationMethodId, certificateUrl,
                    vmEntity.getCertificate()));
        }
        // add common federation verification method
        String commonVerificationMethodId = didWebParticipant + "#JWK2020-PossibleLetsEncrypt";
        didDocument.getVerificationMethod().add(getCommonVerificationMethod(commonVerificationMethodId));

        return didDocument;
    }

    private VerificationMethod getCommonVerificationMethod(String verificationMethodId) throws PemConversionException {

        String controller = getCommonDidWeb();
        String certificateUrl = getDidDocumentUri(getCommonDidWeb()).replace("did.json", "cert.ss.pem");
        return getVerificationMethod(controller, verificationMethodId, certificateUrl, commonCertificateString);
    }

    /**
     * Build the verification method for a given did identifier and string representation of a certificate.
     *
     * @param controller verification method controller
     * @param verificationMethodId verification method identifier
     * @param certificateUrl url to access the certificate
     * @param certificateString certificate in PEM format
     * @return verification method
     * @throws PemConversionException failed to convert the certificate to a public key
     */
    private VerificationMethod getVerificationMethod(String controller, String verificationMethodId,
        String certificateUrl, String certificateString) throws PemConversionException {

        // setup verification method
        VerificationMethod vm = new VerificationMethod();
        vm.setId(verificationMethodId);
        vm.setController(controller);

        // load certificate from string
        X509Certificate x509Certificate;
        try {
            x509Certificate = convertPemStringToCertificate(certificateString);
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
     * @param defaultCertPath path to default certificate
     * @return string representation of certificate
     * @throws CertificateException error during loading of the certificate
     */
    private String getCommonCertificatePemString(String defaultCertPath) throws CertificateException {

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
