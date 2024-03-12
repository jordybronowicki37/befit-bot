package dev.jb.befit.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/hello-world")
@RequiredArgsConstructor
public class HelloWorldController {
    @GetMapping
    public String helloWorld() {
        return "Hello World";
    }
}
