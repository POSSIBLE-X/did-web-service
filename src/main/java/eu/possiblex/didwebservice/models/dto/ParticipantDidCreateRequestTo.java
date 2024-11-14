package eu.possiblex.didwebservice.models.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ParticipantDidCreateRequestTo {
    private String subject;

    private Map<String, String> certificates;
}
