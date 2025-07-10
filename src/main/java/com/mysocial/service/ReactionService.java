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
    
    // Tạo các reaction cố định
    public void initializeDefaultReactions() {
        if (reactionRepository.count() == 0) {
            createReactionHandler("Tim", "LOVE", "/icon/heart.png");
            createReactionHandler("Haha", "HAHA", "/icon/haha.png");
            createReactionHandler("Buồn", "SAD", "/icon/sad.png");
            createReactionHandler("Giận", "ANGRY", "/icon/angry.png");
            createReactionHandler("Wow", "WOW", "/icon/wow.png");
            createReactionHandler("Like", "LIKE", "/icon/like.png");
        }
    }
    
    // Lấy reaction theo type
    public Reaction getReactionByType(String reactionType) {
        return reactionRepository.findByReactionType(reactionType).orElse(null);
    }
}
