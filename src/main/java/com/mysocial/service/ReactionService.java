package com.mysocial.service;

import com.mysocial.model.Reaction;
import com.mysocial.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReactionService {
    @Autowired
    private ReactionRepository reactionRepository;

    public Reaction createReactionHandler(String title, String reactionType, String iconUrl){
        Reaction reaction = new Reaction();
        reaction.setTitle(title);
        reaction.setUrlReaction(iconUrl);
        reaction.setReactionType(reactionType);

        return reactionRepository.save(reaction);
    }
    public List<Reaction> getAllReaction(){
        return reactionRepository.findAll();
    }
}
