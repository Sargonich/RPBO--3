package org.example.controller;

import org.example.annotation.Cumponent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Cumponent
@RestController
@RequestMapping("/api/static-response")
public class StaticHtmlController {

    private static final String HTML = """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <title>Tournament Service</title>
            </head>
            <body>
                <h1>Tournament Service</h1>
                <p>Запрос успешно обработан.</p>
            </body>
            </html>
            """;

    @PostMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handlePost() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(HTML);
    }

    @PutMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handlePut() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(HTML);
    }
}