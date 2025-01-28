package eu.possiblex.didwebservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDidCreateRequestTo {
    private String subject;

    private List<String> aliases;

    private Map<String, String> certificates;
}
