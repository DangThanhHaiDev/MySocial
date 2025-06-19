package com.mysocial.controller;

import com.mysocial.model.Reaction;
import com.mysocial.service.FileService;
import com.mysocial.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {
    @Autowired
    private FileService fileService;
    @Autowired
    private ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Reaction> createReaction(@RequestParam("title") String title,
                                                   @RequestParam("reactionType") String reactionType,
                                                   @RequestParam("icon")MultipartFile icon) throws IOException {
        String urlIcon = fileService.saveIcon(icon);
        Reaction response = reactionService.createReactionHandler(title, reactionType, urlIcon);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<?>  getAllReaction(){
        return new ResponseEntity<>(reactionService.getAllReaction(), HttpStatus.OK);
    }
}
