package eu.possiblex.didwebservice.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDidCreateRequestTo {
    @NotBlank
    @Schema(description = "unique subject identifier which will be used as a basis for generating the did:web", example = "Some Organization Ltd.")
    private String subject;

    @Schema(description = "Optional list of URIs that also reference this identity", example = "[\"https://someorganization.com\"]")
    private List<String> aliases;

    @Schema(description = "Optional map of certificate ids and their contents that should be listed as verification methods in the did document", example = "{\"someorgltd-example-cert\": \"-----BEGIN CERTIFICATE-----\\n...\\n-----END CERTIFICATE-----\"}")
    private Map<String, String> certificates;
}
