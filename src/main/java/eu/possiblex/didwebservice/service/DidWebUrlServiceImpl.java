package eu.possiblex.didwebservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DidWebUrlServiceImpl implements DidWebUrlService {

    private final String didWebDomain;

    public DidWebUrlServiceImpl(@Value("${did-web-domain}") String didWebDomain) {

        this.didWebDomain = didWebDomain;
    }

    /**
     * Build the full did-web path for a given participant id.
     *
     * @param id participant id
     * @return did-web path
     */
    @Override
    public String getDidWebForParticipant(String id) {

        return getCommonDidWeb() + ":participant:" + id;
    }

    /**
     * Get the did-web base path for the currently configured domain.
     *
     * @return did-web base path
     */
    @Override
    public String getCommonDidWeb() {

        return "did:web:" + didWebDomain.replaceFirst(":", "%3A");
    }
}
