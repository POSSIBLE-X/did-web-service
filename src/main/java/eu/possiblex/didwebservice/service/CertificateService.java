/*
 *  Copyright 2024-2025 Dataport. All rights reserved. Developed as part of the POSSIBLE project.
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
