package com.pastebin.controller;

import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.service.PasteService;
import com.pastebin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pastes")
public class ApiController {

    @Autowired
    private PasteService pasteService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Paste>> getAllPastes() {
        List<Paste> pastes = pasteService.findPublicPastes();
        return ResponseEntity.ok(pastes);
    }

    @GetMapping("/{urlKey}")
    public ResponseEntity<?> getPaste(@PathVariable String urlKey) {
        Optional<Paste> pasteOpt = pasteService.findByUrlKey(urlKey);

        if (pasteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Paste paste = pasteOpt.get();

        if (pasteService.isExpired(paste)) {
            return ResponseEntity.status(HttpStatus.GONE).body("Paste has expired");
        }

        if (paste.getIsPrivate()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Private paste");
        }

        return ResponseEntity.ok(paste);
    }

    @PostMapping
    public ResponseEntity<?> createPaste(@RequestBody Map<String, Object> request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();

            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String language = (String) request.getOrDefault("language", "text");
            Boolean isPrivate = (Boolean) request.getOrDefault("isPrivate", false);
            Integer expirationDays = request.get("expirationDays") != null ?
                    Integer.parseInt(request.get("expirationDays").toString()) : null;

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Title is required");
            }

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content is required");
            }

            Paste paste = pasteService.createPaste(title, content, language, isPrivate, expirationDays, user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("urlKey", paste.getUrlKey());
            response.put("url", "/p/" + paste.getUrlKey());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating paste: " + e.getMessage());
        }
    }

    @DeleteMapping("/{urlKey}")
    public ResponseEntity<?> deletePaste(@PathVariable String urlKey, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            Optional<Paste> pasteOpt = pasteService.findByUrlKey(urlKey);

            if (pasteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Paste paste = pasteOpt.get();

            if (!paste.getUser().getId().equals(user.getId()) && !userService.isAdmin(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this paste");
            }

            pasteService.deletePasteByUrlKey(urlKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paste deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting paste: " + e.getMessage());
        }
    }
}
