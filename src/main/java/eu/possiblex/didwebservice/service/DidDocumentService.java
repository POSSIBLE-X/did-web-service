package eu.possiblex.didwebservice.service;

import eu.possiblex.didwebservice.models.did.DidDocument;

public interface DidDocumentService {

    /**
     * Returns the DID document for a given id.
     *
     * @param id id to retrieve the DID document with
     * @return the did document as string
     */
    DidDocument getParticipantDidDocument(String id);

    /**
     * Returns the DID document for the dataspace federation.
     *
     * @return the did document
     */
    DidDocument getCommonDidDocument();
}
