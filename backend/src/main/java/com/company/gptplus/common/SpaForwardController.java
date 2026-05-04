package com.company.gptplus.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
    @GetMapping({"/", "/login", "/user", "/redeem", "/verification-code", "/pay/result", "/product/{id}"})
    public String webApp() {
        return "forward:/index.html";
    }

    @GetMapping({"/admin-ui", "/admin-ui/", "/admin-ui/{path:[^\\.]*}"})
    public String adminApp() {
        return "forward:/admin-ui/index.html";
    }
}
