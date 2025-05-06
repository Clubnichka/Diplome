package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.service.TagDictionary;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@CrossOrigin(origins = "http://localhost:3000")
public class TagController {

    @GetMapping
    public List<String> getAllTags() {
        return TagDictionary.DEFAULT_TAGS;
    }
}