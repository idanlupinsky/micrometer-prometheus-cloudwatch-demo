package com.distilledcourses.micrometerdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {
    @GetMapping("/demo")
    @ResponseBody
    public void doSomeWork() throws InterruptedException {
        Thread.sleep(100);
    }
}
