package com.fincity.saas.commons.configuration;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincity.saas.commons.configuration.service.AbstractMessageService;
import com.fincity.saas.commons.exeception.GenericException;

import feign.FeignException;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@Priority(0)
public class ControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    @Autowired
    private AbstractMessageService resourceService;

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<Object> handleGenericException(GenericException ex) {
        logger.debug("Generic Exception Occurred : ", ex);
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.toExceptionData());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException fe) {
        logger.debug("Feign Exception Occurred : ", fe);
        Optional<ByteBuffer> byteBuffer = fe.responseBody();
        if (byteBuffer.isPresent() && byteBuffer.get().hasArray()) {
            Collection<String> ctype = fe.responseHeaders().get(HttpHeaders.CONTENT_TYPE);
            if (ctype != null && ctype.contains("application/json")) {
                try {
                    Map<String, Object> map = this.objectMapper.readValue(byteBuffer.get().array(),
                            new TypeReference<Map<String, Object>>() {});
                    GenericException g = new GenericException(
                            HttpStatus.valueOf(fe.status()),
                            map.get("message") == null ? "" : map.get("message").toString(),
                            fe);
                    return ResponseEntity.status(g.getStatusCode())
                            .body(g.toExceptionData());
                } catch (Exception e) {
                    logger.error("Error parsing Feign exception response", e);
                }
            }
        }
        return handleOtherExceptions(fe);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOtherExceptions(Throwable ex) {
        String eId = GenericException.uniqueId();
        String msg = resourceService.getMessage(AbstractMessageService.UNKNOWN_ERROR_WITH_ID, eId);

        log.error("Error : {}", eId, ex);

        final HttpStatus status = (ex instanceof ResponseStatusException rse)
                ? HttpStatus.valueOf(rse.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        GenericException g = new GenericException(status, eId, msg, ex);
        return ResponseEntity.status(g.getStatusCode())
                .body(g.toExceptionData());
    }
}