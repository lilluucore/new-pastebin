package com.pastebin.controller;

import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.service.PasteService;
import com.pastebin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class PasteController {

    @Autowired
    private PasteService pasteService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        List<Paste> userPastes = pasteService.findUserPastes(user);
        model.addAttribute("pastes", userPastes);
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @GetMapping("/new")
    public String newPaste() {
        return "new-paste";
    }

    @PostMapping("/paste")
    public String createPaste(@RequestParam String title,
                              @RequestParam String content,
                              @RequestParam(defaultValue = "text") String language,
                              @RequestParam(defaultValue = "false") Boolean isPrivate,
                              @RequestParam(required = false) Integer expirationDays,
                              Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        Paste paste = pasteService.createPaste(title, content, language, isPrivate, expirationDays, user);
        return "redirect:/p/" + paste.getUrlKey();
    }

    @GetMapping("/p/{urlKey}")
    public String viewPaste(@PathVariable String urlKey, Authentication authentication, Model model) {
        Optional<Paste> pasteOpt = pasteService.findByUrlKey(urlKey);

        if (pasteOpt.isEmpty()) {
            return "error";
        }

        Paste paste = pasteOpt.get();

        if (pasteService.isExpired(paste)) {
            return "error";
        }

        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByUsername(authentication.getName()).orElse(null);
        }

        if (!pasteService.canUserAccessPaste(paste, currentUser)) {
            return "error";
        }

        pasteService.incrementViewCount(paste);

        model.addAttribute("paste", paste);
        model.addAttribute("currentUser", currentUser);
        return "view-paste";
    }

    @PostMapping("/paste/{id}/delete")
    public String deletePaste(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        Optional<Paste> pasteOpt = pasteService.findByUrlKey(id.toString());

        if (pasteOpt.isPresent() && pasteOpt.get().getUser().getId().equals(user.getId())) {
            pasteService.deletePaste(id);
        }

        return "redirect:/dashboard";
    }
}
