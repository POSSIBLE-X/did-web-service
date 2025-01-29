package eu.possiblex.didwebservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseTO {
    private OffsetDateTime timestamp;

    private String message;

    private String details;

    public ErrorResponseTO(String message) {

        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.details = "";
    }

    public ErrorResponseTO(String message, String details) {

        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.details = details;
    }
}
