package com.logic.project.controller;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> state(IllegalStateException e){
        return ResponseEntity.status(401).body(Map.of("status",401,"message",e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<?> security(SecurityException e){
        return ResponseEntity.status(403).body(Map.of("status",403,"message",e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> argument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(Map.of("status",400,"message",e.getMessage()));
    }
}
