package com.cinebee.application.util;

import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.shared.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    @Autowired
    private UserRepository userRepository;

    public String generateBaseUsername(String fullName) {
        String baseUsername = UserUtils.removeVietnameseTones(fullName.trim().split("\\s+")[0].toLowerCase());
        String username;
        do {
            int randomNum = (int) (Math.random() * 1_000_000_000);
            username = baseUsername + randomNum;
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }

    public String generateGoogleUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int i = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + i;
            i++;
        }
        return username;
    }
}

