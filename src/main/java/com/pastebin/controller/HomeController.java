package com.pastebin.controller;

import com.pastebin.model.Paste;
import com.pastebin.service.PasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PasteService pasteService;

    @GetMapping("/")
    public String home(Model model) {
        List<Paste> recentPastes = pasteService.findPublicPastes();
        model.addAttribute("recentPastes", recentPastes);
        return "index";
    }
}
