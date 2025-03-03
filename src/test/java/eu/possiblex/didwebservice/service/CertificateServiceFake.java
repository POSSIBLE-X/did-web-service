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

import eu.possiblex.didwebservice.models.exceptions.CertificateNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateServiceFake implements CertificateService {
    public static final String EXAMPLE_CERTIFICATE = """
        -----BEGIN CERTIFICATE-----
        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
        DA==
        -----END CERTIFICATE-----
        """;

    @Override
    public String getParticipantCertificate(String participantId, String certId) {

        if (participantId.equals("unknown-participant")) {
            throw new ParticipantNotFoundException("not found");
        }

        if (certId.equals("unknown-certificate")) {
            throw new CertificateNotFoundException("not found");
        }

        return EXAMPLE_CERTIFICATE;
    }

    @Override
    public String getCommonCertificate() {

        return EXAMPLE_CERTIFICATE;
    }

    @Override
    public X509Certificate convertPemStringToCertificate(String certs) throws CertificateException {

        ByteArrayInputStream certStream = new ByteArrayInputStream(
            EXAMPLE_CERTIFICATE.getBytes(StandardCharsets.UTF_8));

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        return ((List<X509Certificate>) certFactory.generateCertificates(certStream)).stream().findFirst().orElse(null);
    }
}
