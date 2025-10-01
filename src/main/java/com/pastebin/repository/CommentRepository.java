package com.pastebin.repository;

import com.pastebin.model.Comment;
import com.pastebin.model.Paste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.paste = :paste AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPaste(Paste paste);

    List<Comment> findByPasteOrderByCreatedAtAsc(Paste paste);
}
