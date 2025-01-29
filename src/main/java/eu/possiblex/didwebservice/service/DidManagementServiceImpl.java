package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.dto.ParticipantDidCreateRequestTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidTo;
import eu.possiblex.didwebservice.models.dto.ParticipantDidUpdateRequestTo;
import eu.possiblex.didwebservice.models.entities.ParticipantDidDataEntity;
import eu.possiblex.didwebservice.models.entities.VerificationMethodEntity;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import eu.possiblex.didwebservice.repositories.ParticipantDidDataRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.*;

@Service
@Slf4j
public class DidManagementServiceImpl implements DidManagementService {

    private final ParticipantDidDataRepository participantDidDataRepository;

    private final CertificateService certificateService;

    private final DidWebUrlService didWebUrlService;

    private final boolean commonVmEnabled;

    private final String commonVmId;

    public DidManagementServiceImpl(@Value("${common-verification-method.enabled:#{null}}") boolean commonVmEnabled,
        @Value("${common-verification-method.id:#{null}}") String commonVmId,
        @Autowired ParticipantDidDataRepository participantDidDataRepository,
        @Autowired CertificateService certificateService, @Autowired DidWebUrlService didWebUrlService) {

        this.commonVmEnabled = commonVmEnabled;
        this.commonVmId = commonVmId;
        this.participantDidDataRepository = participantDidDataRepository;
        this.certificateService = certificateService;
        this.didWebUrlService = didWebUrlService;
    }

    /**
     * Generates a did:web entry. Returns the did:web and the verification methods.
     *
     * @param request with information needed for certificate generation
     * @return dto containing the generated did:web and the verification methods
     */
    @Transactional
    public ParticipantDidTo generateParticipantDidWeb(ParticipantDidCreateRequestTo request) {

        String certificateSubject = request.getSubject();

        if (certificateSubject == null || certificateSubject.isBlank()) {
            throw new RequestArgumentException("Missing or empty subject name.");
        }

        String didWeb = generateDidWeb(certificateSubject);

        ParticipantDidDataEntity entity = storeDidDocument(didWeb, request.getCertificates(), request.getAliases());

        return new ParticipantDidTo(entity.getDid(), getVmIdsFromParticipantEntity(entity), entity.getAliases());
    }

    /**
     * Updates an existing did:web with new content.
     *
     * @param request updated information, null for info that should stay the same
     */
    @Override
    @Transactional
    public ParticipantDidTo updateParticipantDidWeb(ParticipantDidUpdateRequestTo request) {

        String didWeb = request.getDid();

        if (didWeb == null || didWeb.isBlank()) {
            throw new RequestArgumentException("Missing or empty did.");
        }

        ParticipantDidDataEntity entity = participantDidDataRepository.findByDid(didWeb);

        if (entity == null) {
            throw new ParticipantNotFoundException("Did does not exist in the database.");
        }

        if (request.getAliases() != null) {
            entity.setAliases(request.getAliases());
        }

        if (request.getCertificates() != null) {
            entity.setVerificationMethods(getVerificationMethodEntities(request.getCertificates()));
        }

        return new ParticipantDidTo(entity.getDid(), getVmIdsFromParticipantEntity(entity), entity.getAliases());
    }

    /**
     * Removes an existing did:web if it exists.
     *
     * @param did did to remove
     */
    @Transactional
    @Override
    public void removeParticipantDidWeb(String did) {

        if (did == null || did.isBlank()) {
            throw new RequestArgumentException("Missing or empty did.");
        }

        deleteDidDocument(did);
    }

    private List<String> getVmIdsFromParticipantEntity(ParticipantDidDataEntity entity) {

        List<String> verificationMethodIds = new ArrayList<>(
            entity.getVerificationMethods().stream().map(vm -> entity.getDid() + "#" + vm.getCertificateId()).toList());
        if (commonVmEnabled) {
            verificationMethodIds.add(entity.getDid() + "#" + commonVmId);
        }
        return verificationMethodIds;
    }

    private void deleteDidDocument(String did) {

        if (participantDidDataRepository.findByDid(did) == null) {
            log.info("Did {} does not exist in the database.", did);
            return;
        }
        participantDidDataRepository.deleteByDid(did);
    }

    private List<VerificationMethodEntity> getVerificationMethodEntities(Map<String, String> certificates) {

        if (certificates == null) {
            return Collections.emptyList();
        }

        List<VerificationMethodEntity> verificationMethods = new ArrayList<>();
        for (var certEntry : certificates.entrySet()) {
            try {
                certificateService.convertPemStringToCertificate(certEntry.getValue());
            } catch (CertificateException e) {
                throw new RequestArgumentException("Certificate with ID " + certEntry.getKey() + " is not valid.");
            }

            if (!certEntry.getKey().matches("^[0-9A-Za-z-]+$")) {
                throw new RequestArgumentException("Certificate has invalid characters in ID: " + certEntry.getKey());
            }

            if (commonVmEnabled && certEntry.getKey().equals(commonVmId)) {
                throw new RequestArgumentException(
                    "Certificate ID " + certEntry.getKey() + " is reserved for common verification method.");
            }

            VerificationMethodEntity verificationMethodEntity = new VerificationMethodEntity();
            verificationMethodEntity.setCertificateId(certEntry.getKey());
            verificationMethodEntity.setCertificate(certEntry.getValue());
            verificationMethods.add(verificationMethodEntity);
        }
        return verificationMethods;
    }

    /**
     * generate a did-web identifier based on the given seed.
     *
     * @param seed seed to generate the did-web from
     * @return generated did-web identifier
     */
    private String generateDidWeb(String seed) {

        String uuid = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
        return didWebUrlService.getDidWebForParticipant(uuid);
    }

    /**
     * Create a new database entry for the given did if it does not already exist. If it already exists, log it and do
     * nothing.
     *
     * @param did did to store in the database
     */
    private ParticipantDidDataEntity storeDidDocument(String did, Map<String, String> certificates,
        List<String> aliases) {

        ParticipantDidDataEntity data = participantDidDataRepository.findByDid(did);
        if (data != null) {
            log.info("Did {} already exists in the database.", did);
            return data;
        }
        data = new ParticipantDidDataEntity();
        data.setDid(did);

        data.setVerificationMethods(getVerificationMethodEntities(certificates));

        if (aliases != null) {
            data.setAliases(aliases);
        }

        return participantDidDataRepository.save(data);
    }
}
