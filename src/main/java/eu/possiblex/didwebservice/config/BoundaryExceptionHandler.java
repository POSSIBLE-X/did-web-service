/*
 *  Copyright 2024-2025 Dataport. All rights reserved. Developed as part of the POSSIBLE project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
