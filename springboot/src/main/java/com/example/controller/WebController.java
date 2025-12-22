package com.example.controller;

import com.example.common.Result;
import org.springframework.web.bind.annotation.*;

/**
 * Basic web interface - simplified for local research tool
 */
@RestController
public class WebController {
    
    @GetMapping("/")
    public Result<String> hello() {
        return Result.success("Literature Manager API - Local Research Tool");
    }

}
