package eu.possiblex.didwebservice.utils;

public class DidUtils {

    public static final String DID_WEB_PREFIX = "did:web:";

    public static final String DID_DOCUMENT_FILE = "did.json";

    public static final String COMMON_CERTIFICATE_FILE = "cert.ss.pem";

    private DidUtils() {

    }

    /**
     * Given a valid did:web, return the resulting URI to the did document. See <a
     * href="https://w3c-ccg.github.io/did-method-web/#read-resolve">did-web specification</a> for reference.
     *
     * @param didWeb did:web
     * @return did web URI
     */
    public static String getDidDocumentUri(String didWeb) {

        String didWebWithoutPrefix = didWeb.replace(DID_WEB_PREFIX, "");
        boolean containsSubpath = didWebWithoutPrefix.contains(":");
        StringBuilder didDocumentUriBuilder = new StringBuilder();
        didDocumentUriBuilder.append(
            didWebWithoutPrefix.replace(":", "/") // Replace ":" with "/" in the method specific identifier to
                // obtain the fully qualified domain name and optional path.
                .replace("%3A", ":")); // If the domain contains a port percent decode the colon.

        // Generate an HTTPS URL to the expected location of the DID document by prepending https://.
        didDocumentUriBuilder.insert(0, "https://");
        if (!containsSubpath) {
            // If no path has been specified in the URL, append /.well-known.
            didDocumentUriBuilder.append("/.well-known");
        }
        // Append /did.json to complete the URL.
        didDocumentUriBuilder.append("/" + DID_DOCUMENT_FILE);

        return didDocumentUriBuilder.toString();
    }
}
