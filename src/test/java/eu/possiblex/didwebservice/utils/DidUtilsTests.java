package eu.possiblex.didwebservice.utils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DidUtilsTests {

    private static Stream<Arguments> provideDidWebParameters() {

        return Stream.of(Arguments.of("did:web:example.com", "https://example.com/.well-known/did.json"),
            Arguments.of("did:web:localhost%3A1234", "https://localhost:1234/.well-known/did.json"),
            Arguments.of("did:web:example.com:some:path:123", "https://example.com/some/path/123/did.json"));
    }

    @ParameterizedTest
    @MethodSource("provideDidWebParameters")
    void generateDidWebUrlCorrectly(String didWebString, String didWebUrlTarget) {

        String didWebUrl = DidUtils.getDidDocumentUri(didWebString);
        assertEquals(didWebUrlTarget, didWebUrl);
    }
}
