package com.pastebin.controller;

import com.pastebin.model.Paste;
import com.pastebin.model.User;
import com.pastebin.service.CommentService;
import com.pastebin.service.PasteService;
import com.pastebin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasteService pasteService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public String adminPanel(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();

        if (!userService.isAdmin(currentUser)) {
            return "redirect:/";
        }

        List<User> allUsers = userService.findAllUsers();
        List<Paste> allPastes = pasteService.findAllPastes();

        long totalUsers = allUsers.size();
        long totalPastes = allPastes.size();
        long totalComments = allPastes.stream()
                .mapToLong(paste -> paste.getComments().size())
                .sum();
        long totalViews = allPastes.stream()
                .mapToLong(Paste::getViewCount)
                .sum();

        model.addAttribute("users", allUsers);
        model.addAttribute("pastes", allPastes);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPastes", totalPastes);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("currentUser", currentUser);

        return "admin";
    }

    @PostMapping("/user/{id}/ban")
    @ResponseBody
    public String banUser(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();

        if (!userService.isAdmin(currentUser)) {
            return "error";
        }

        if (currentUser.getId().equals(id)) {
            return "error";
        }

        try {
            userService.banUser(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/user/{id}/unban")
    @ResponseBody
    public String unbanUser(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();

        if (!userService.isAdmin(currentUser)) {
            return "error";
        }

        try {
            userService.unbanUser(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/paste/{urlKey}/delete")
    @ResponseBody
    public String deletePaste(@PathVariable String urlKey, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();

        if (!userService.isAdmin(currentUser)) {
            return "error";
        }

        try {
            pasteService.deletePasteByUrlKey(urlKey);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}
