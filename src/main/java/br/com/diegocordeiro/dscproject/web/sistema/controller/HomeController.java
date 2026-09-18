package br.com.diegocordeiro.dscproject.web.sistema.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        return "redirect:/dashboards";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
