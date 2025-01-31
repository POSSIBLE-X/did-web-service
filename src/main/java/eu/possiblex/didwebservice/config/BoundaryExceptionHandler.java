package eu.possiblex.didwebservice.config;

import eu.possiblex.didwebservice.models.dto.ErrorResponseTO;
import eu.possiblex.didwebservice.models.exceptions.CertificateNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.DidDocumentGenerationException;
import eu.possiblex.didwebservice.models.exceptions.ParticipantNotFoundException;
import eu.possiblex.didwebservice.models.exceptions.RequestArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

/**
 * Exception handler for boundary exceptions that should be passed as API response.
 */
@RestControllerAdvice
@Slf4j
public class BoundaryExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle exceptions that occur when the DID document could not be generated.
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseTO> handleException(DidDocumentGenerationException e) {

        logError(e);
        return new ResponseEntity<>(new ErrorResponseTO("Failed to generate requested DID document", e.getMessage()),
            INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle exceptions that occur when a referenced participant is not found.
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseTO> handleException(ParticipantNotFoundException e) {

        logError(e);
        return new ResponseEntity<>(new ErrorResponseTO("Requested participant was not found", e.getMessage()),
            NOT_FOUND);
    }

    /**
     * Handle exceptions that occur when a referenced certificate is not found.
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseTO> handleException(CertificateNotFoundException e) {

        logError(e);
        return new ResponseEntity<>(new ErrorResponseTO("Requested certificate was not found", e.getMessage()),
            NOT_FOUND);
    }

    /**
     * Handle exceptions that occur when an invalid management request was sent.
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseTO> handleException(RequestArgumentException e) {

        logError(e);
        return new ResponseEntity<>(new ErrorResponseTO("Failed to process management request.", e.getMessage()),
            BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseTO> handleException(Exception e) {

        logError(e);
        return new ResponseEntity<>(new ErrorResponseTO("An unknown error occurred"), INTERNAL_SERVER_ERROR);
    }

    private void logError(Exception e) {

        log.error("Caught boundary exception: {}", e.getClass().getName(), e);
    }

}
