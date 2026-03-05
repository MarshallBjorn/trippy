package com.navrotskyi.trippyapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of("status", "ok", "message", "Trippy API nuh uh hotreload doesnt work ;p");
    }
}
