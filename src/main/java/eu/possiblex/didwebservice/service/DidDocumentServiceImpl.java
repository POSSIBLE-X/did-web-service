package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.did.DidDocument;
import eu.possiblex.didwebservice.models.did.PublicJwk;
import eu.possiblex.didwebservice.models.did.VerificationMethod;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.PemConversionException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import eu.possiblex.didwebservice.utils.DidUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Service
@Slf4j
public class DidDocumentServiceImpl implements DidDocumentService {

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final DidWebUrlService didWebUrlService;

    private final CertificateService certificateService;

    private final boolean commonVmEnabled;

    private final String commonVmId;

    public DidDocumentServiceImpl(@Value("${common-verification-method.enabled:#{null}}") boolean commonVmEnabled,
        @Value("${common-verification-method.id:#{null}}") String commonVmId,
        @Autowired ParticipantDidDataRepository participantDidDataRepository,
        @Autowired DidWebUrlService didWebUrlService, @Autowired CertificateService certificateService) {

        this.commonVmEnabled = commonVmEnabled;
        this.commonVmId = commonVmId;
        this.participantDidDataRepository = participantDidDataRepository;
        this.didWebUrlService = didWebUrlService;
        this.certificateService = certificateService;
    }

    /**
     * Get the DID document for a given participant DID.
     *
     * @param id id to retrieve the DID document for
     * @return did document
     */
    @Override
    public DidDocument getParticipantDidDocument(String id) {

        String didWeb = didWebUrlService.getDidWebForParticipant(id);

        ParticipantDidDataEntity participantDidDataEntity = participantDidDataRepository.findByDid(didWeb);

        if (participantDidDataEntity == null) {
            throw new ParticipantNotFoundException("Participant could not be found.");
        }

        try {
            return buildDidDocumentFromEntity(participantDidDataEntity);
        } catch (Exception e) {
            throw new DidDocumentGenerationException(e.getMessage());
        }
    }

    /**
     * Get the common DID document for the federation.
     *
     * @return federation did document
     */
    @Override
    public DidDocument getCommonDidDocument() {

        try {
            ParticipantDidDataEntity federationCert = new ParticipantDidDataEntity();
            federationCert.setDid(didWebUrlService.getCommonDidWeb());
            return buildDidDocumentFromEntity(federationCert);
        } catch (PemConversionException e) {
            throw new DidDocumentGenerationException("Failed to build did.json for Federation: " + e.getMessage());
        }

    }

    /**
     * Given a database entry for a participant, build the corresponding did document.
     *
     * @param participantDidDataEntity participant data to build the did document for
     * @return JSON string representation of the did document
     */
    private DidDocument buildDidDocumentFromEntity(ParticipantDidDataEntity participantDidDataEntity) {

        // get did identifier from db data
        String didWebParticipant = participantDidDataEntity.getDid();

        // build did document
        DidDocument didDocument = new DidDocument();
        didDocument.setId(didWebParticipant);
        didDocument.setAlsoKnownAs(participantDidDataEntity.getAliases());

        for (VerificationMethodEntity vmEntity : participantDidDataEntity.getVerificationMethods()) {
            String certificateUrl = DidUtils.getDidDocumentUri(didWebParticipant)
                .replace(DidUtils.DID_DOCUMENT_FILE, vmEntity.getCertificateId() + ".pem");
            String verificationMethodId = didWebParticipant + "#" + vmEntity.getCertificateId();
            didDocument.getVerificationMethod().add(
                getVerificationMethod(didWebParticipant, verificationMethodId, certificateUrl,
                    vmEntity.getCertificate()));
        }
        if (commonVmEnabled) {
            // add common federation verification method
            String commonVerificationMethodId = didWebParticipant + "#" + commonVmId;
            didDocument.getVerificationMethod().add(getCommonVerificationMethod(commonVerificationMethodId));
        }

        return didDocument;
    }

    private VerificationMethod getCommonVerificationMethod(String verificationMethodId) {

        String controller = didWebUrlService.getCommonDidWeb();
        String certificateUrl = DidUtils.getDidDocumentUri(didWebUrlService.getCommonDidWeb())
            .replace(DidUtils.DID_DOCUMENT_FILE, DidUtils.COMMON_CERTIFICATE_FILE);
        return getVerificationMethod(controller, verificationMethodId, certificateUrl,
            certificateService.getCommonCertificate());
    }

    /**
     * Build the verification method for a given did identifier and string representation of a certificate.
     *
     * @param controller verification method controller
     * @param verificationMethodId verification method identifier
     * @param certificateUrl url to access the certificate
     * @param certificateString certificate in PEM format
     * @return verification method
     */
    private VerificationMethod getVerificationMethod(String controller, String verificationMethodId,
        String certificateUrl, String certificateString) {

        // setup verification method
        VerificationMethod vm = new VerificationMethod();
        vm.setId(verificationMethodId);
        vm.setController(controller);

        // load certificate from string
        X509Certificate x509Certificate;
        try {
            x509Certificate = certificateService.convertPemStringToCertificate(certificateString);
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
}
