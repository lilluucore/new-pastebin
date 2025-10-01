package com.pastebin.controller;

import com.pastebin.model.Comment;
import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.service.CommentService;
import com.pastebin.service.PasteService;
import com.pastebin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PasteService pasteService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody Map<String, Object> request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();

            String content = (String) request.get("content");
            String urlKey = (String) request.get("pasteUrlKey");
            Long parentId = request.get("parentId") != null ?
                    Long.parseLong(request.get("parentId").toString()) : null;

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content is required");
            }

            Optional<Paste> pasteOpt = pasteService.findByUrlKey(urlKey);
            if (pasteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Paste paste = pasteOpt.get();
            Comment parent = null;

            if (parentId != null) {
                parent = commentService.findById(parentId).orElse(null);
            }

            Comment comment = commentService.createComment(content, paste, user, parent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("commentId", comment.getId());
            response.put("username", user.getUsername());
            response.put("content", comment.getContent());
            response.put("createdAt", comment.getCreatedAt().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating comment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            Optional<Comment> commentOpt = commentService.findById(id);

            if (commentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Comment comment = commentOpt.get();

            if (!comment.getUser().getId().equals(user.getId()) && !userService.isAdmin(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this comment");
            }

            commentService.deleteComment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting comment: " + e.getMessage());
        }
    }
}
