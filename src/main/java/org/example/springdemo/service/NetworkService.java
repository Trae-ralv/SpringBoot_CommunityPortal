package org.example.springdemo.service;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NetworkService {

    private final AppRepository userRepository;

    @Autowired
    public NetworkService(AppRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserModel> searchAllUsers(String query) {
        List<UserModel> allUsers = userRepository.findAll();

        if (query == null || query.trim().isEmpty()) {
            return allUsers;
        }

        String searchTerm = query.trim().toLowerCase();
        return allUsers.stream()
                .filter(user ->
                        (user.getF_name() + " " + user.getL_name()).toLowerCase().contains(searchTerm) ||
                                user.getEmail().toLowerCase().contains(searchTerm))
                .toList();
    }
}