package eu.possiblex.didwebservice.models.dto;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantDidUpdateRequestTo {
    private String did;

    private List<String> aliases;
}
