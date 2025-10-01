package com.pastebin.service;

import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.repository.PasteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PasteService {

    @Autowired
    private PasteRepository pasteRepository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int URL_KEY_LENGTH = 8;

    public Paste createPaste(String title, String content, String language, Boolean isPrivate,
                             Integer expirationDays, User user) {
        Paste paste = new Paste();
        paste.setTitle(title);
        paste.setContent(content);
        paste.setLanguage(language);
        paste.setIsPrivate(isPrivate);
        paste.setUser(user);
        paste.setUrlKey(generateUniqueUrlKey());

        if (expirationDays != null && expirationDays > 0) {
            paste.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
        }

        return pasteRepository.save(paste);
    }

    private String generateUniqueUrlKey() {
        String urlKey;
        do {
            urlKey = generateRandomString(URL_KEY_LENGTH);
        } while (pasteRepository.existsByUrlKey(urlKey));
        return urlKey;
    }

    private String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public Optional<Paste> findByUrlKey(String urlKey) {
        return pasteRepository.findByUrlKey(urlKey);
    }

    @Transactional
    public void incrementViewCount(Paste paste) {
        paste.setViewCount(paste.getViewCount() + 1);
        pasteRepository.save(paste);
    }

    public List<Paste> findAllPastes() {
        return pasteRepository.findAll();
    }

    @Transactional
    public void deletePasteByUrlKey(String urlKey) {
        pasteRepository.findByUrlKey(urlKey).ifPresent(paste -> pasteRepository.delete(paste));
    }

    public List<Paste> findPublicPastes() {
        LocalDateTime now = LocalDateTime.now();
        return pasteRepository.findPublicAndNotExpired(now);
    }

    public List<Paste> findUserPastes(User user) {
        LocalDateTime now = LocalDateTime.now();
        return pasteRepository.findByUserAndNotExpired(user, now);
    }

    public void deletePaste(Long id) {
        pasteRepository.deleteById(id);
    }

    public boolean canUserAccessPaste(Paste paste, User currentUser) {
        if (!paste.getIsPrivate()) {
            return true;
        }
        if (currentUser == null) {
            return false;
        }
        return paste.getUser().getId().equals(currentUser.getId());
    }

    public boolean isExpired(Paste paste) {
        if (paste.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(paste.getExpiresAt());
    }
}
