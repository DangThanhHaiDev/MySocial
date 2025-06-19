package com.mysocial.service;

import com.mysocial.model.PostReaction;
import com.mysocial.repository.PostReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostReactionService {
    @Autowired
    private PostReactionRepository postReactionRepository;

//    public PostReaction createEmotionPost(){
//
//    }
}
