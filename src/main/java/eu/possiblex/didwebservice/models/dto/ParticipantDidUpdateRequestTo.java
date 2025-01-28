package eu.possiblex.didwebservice.models.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ParticipantDidUpdateRequestTo {
    private String did;

    private List<String> aliases;

    private Map<String, String> certificates;
}
