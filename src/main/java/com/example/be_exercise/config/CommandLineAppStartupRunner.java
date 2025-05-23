package com.example.be_exercise.config;

import com.example.be_exercise.model.Role;
import com.example.be_exercise.model.User;
import com.example.be_exercise.repository.RoleRepository;
import com.example.be_exercise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default role USER, ADMIN
        if (!roleRepository.existsByName(Role.ADMIN)) {
            Role role = Role.builder()
                    .name(Role.ADMIN)
                    .build();
            roleRepository.save(role);
        }
        if (!roleRepository.existsByName(Role.USER)) {
            Role role = Role.builder()
                    .name(Role.USER)
                    .build();
            roleRepository.save(role);
        }

        // Create default user admin
        if (!userRepository.existsByUsername("admin")) {
            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@gmail.com")
                    .firstName("Huynh")
                    .lastName("Phat")
                    .build();

            Set<Role> roles = new HashSet<>();
            roleRepository.findByName(Role.ADMIN).ifPresent(roles::add);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}
