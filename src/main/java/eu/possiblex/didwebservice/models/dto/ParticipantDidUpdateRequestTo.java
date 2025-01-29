package eu.possiblex.didwebservice.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDidUpdateRequestTo {
    @NotBlank
    @Schema(description = "did:web identity that is enrolled in this did-web-service and which should be updated.", example = "did:web:example.com:participant:someorgltd")
    private String did;

    @Schema(description = "Optional list of URIs that also reference this identity. Ignored if null.", example = "[\"https://someorganization.com\"]")
    private List<String> aliases;

    @Schema(description = "Optional map of certificate ids and their contents that should be listed as verification methods in the did document. Ignored if null.", example = "{\"someorgltd-example-cert\": \"-----BEGIN CERTIFICATE-----\\n..\\n.-----END CERTIFICATE-----\"}")
    private Map<String, String> certificates;
}
