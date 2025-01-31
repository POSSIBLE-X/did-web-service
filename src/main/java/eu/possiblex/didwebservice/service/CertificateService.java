package eu.possiblex.didwebservice.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface CertificateService {

    /**
     * Returns a specific certificate for a participant.
     *
     * @param participantId id of the participant
     * @param certId id of the certificate
     * @return the certificate
     */
    String getParticipantCertificate(String participantId, String certId);

    /**
     * Returns a certificate for the federation.
     *
     * @return the certificate
     */
    String getCommonCertificate();

    /**
     * Convert a PEM string to a X509 certificate object.
     *
     * @param certs PEM string representation of the certificate
     * @return X509 certificate object
     * @throws CertificateException error during conversion of the certificate
     */
    X509Certificate convertPemStringToCertificate(String certs) throws CertificateException;
}
