package eu.possiblex.didwebservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDidTo {
    private String did;

    private List<String> verificationMethodIds;

    private List<String> aliases;
}
