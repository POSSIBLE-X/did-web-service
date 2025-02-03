package eu.possiblex.didwebservice.service;

public class DidWebUrlServiceFake implements DidWebUrlService {

    private final String didWebDomain;

    public DidWebUrlServiceFake(String didWebDomain) {

        this.didWebDomain = didWebDomain;
    }

    @Override
    public String getDidWebForParticipant(String id) {

        return getCommonDidWeb() + ":participant:" + id;
    }

    @Override
    public String getCommonDidWeb() {

        return "did:web:" + didWebDomain.replaceFirst(":", "%3A");
    }
}
