package com.pastebin.service;

import com.pastebin.model.Comment;
import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public Comment createComment(String content, Paste paste, User user, Comment parent) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (paste == null || user == null) {
            throw new IllegalArgumentException("Paste and user are required");
        }

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setPaste(paste);
        comment.setUser(user);
        comment.setParent(parent);

        return commentRepository.save(comment);
    }

    public List<Comment> getRootCommentsByPaste(Paste paste) {
        return commentRepository.findRootCommentsByPaste(paste);
    }

    public List<Comment> getAllCommentsByPaste(Paste paste) {
        return commentRepository.findByPasteOrderByCreatedAtAsc(paste);
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
