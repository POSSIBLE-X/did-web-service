package eu.possiblex.didwebservice.service;

import ch.qos.logback.core.util.StringUtil;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import eu.possiblex.didwebservice.utils.DidUtils;
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
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final DidWebUrlService didWebUrlService;

    private final String commonCertificateContent;

    public CertificateServiceImpl(@Value("${common-verification-method.enabled:#{null}}") boolean commonVmEnabled,
        @Value("${common-verification-method.cert-path:#{null}}") String commonCertPath,
        @Autowired ParticipantDidDataRepository participantDidDataRepository,
        @Autowired DidWebUrlService didWebUrlService) throws CertificateException {

        this.commonCertificateContent = commonVmEnabled ? getCommonCertificatePemString(commonCertPath) : null;
        this.participantDidDataRepository = participantDidDataRepository;
        this.didWebUrlService = didWebUrlService;
    }

    /**
     * Get the content of a specific certificate for a participant in PEM format.
     *
     * @param participantId id of the participant
     * @param certId id of the certificate
     * @return certificate
     */
    public String getParticipantCertificate(String participantId, String certId) {

        String didWeb = didWebUrlService.getDidWebForParticipant(participantId);

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

        return commonCertificateContent;
    }

    /**
     * Convert a PEM string to a X509 certificate object.
     *
     * @param certs PEM string representation of the certificate
     * @return X509 certificate object
     * @throws CertificateException error during conversion of the certificate
     */
    @SuppressWarnings("unchecked")
    @Override
    public X509Certificate convertPemStringToCertificate(String certs) throws CertificateException {

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
            ? CertificateServiceImpl.class.getClassLoader().getResourceAsStream(DidUtils.COMMON_CERTIFICATE_FILE)
            : new FileInputStream(defaultCertPath)) {
            return new String(
                Objects.requireNonNull(certificateStream, "Certificate input stream is null.").readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new CertificateException("Failed to read common certificate. " + e.getMessage());
        }
    }
}
