package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class DemoController {

    private final DemoService demoService;

    @GetMapping("/demo")
    public String demo() throws Exception {
        demoService.operation1();
        demoService.operation2(Math.random());
        return "micrometer-tracer-demo";
    }
}
