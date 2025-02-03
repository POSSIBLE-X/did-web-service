package eu.possiblex.didwebservice.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipantDidTo {
    @Schema(description = "Generated did:web identity.", example = "did:web:example.com:participant:someorgltd")
    private String did;

    @Schema(description = "List of verification method IDs associated with this identity.", example = "[\"did:web:example.com:participant:someorgltd#someorgltd-example-cert\"]")
    private List<String> verificationMethodIds;

    @Schema(description = "List of aliases associated with this identity.", example = "[\"https://someorganization.com\"]")
    private List<String> aliases;
}
