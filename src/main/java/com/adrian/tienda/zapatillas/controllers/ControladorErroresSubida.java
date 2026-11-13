package com.adrian.tienda.zapatillas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ControladorErroresSubida {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> gestionarSubidaDemasiadoGrande(MaxUploadSizeExceededException error) {
        return ResponseEntity.status(HttpStatus.valueOf(413))
                .body("La subida supera el límite total permitido. Reduce el tamaño o el número de imágenes.");
    }
}
