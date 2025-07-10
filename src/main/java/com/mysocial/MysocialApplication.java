package com.mysocial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import com.mysocial.service.ReactionService;

@SpringBootApplication
public class MysocialApplication implements CommandLineRunner {

	@Autowired
	private ReactionService reactionService;

	public static void main(String[] args) {
		SpringApplication.run(MysocialApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactionService.initializeDefaultReactions();
	}
}



