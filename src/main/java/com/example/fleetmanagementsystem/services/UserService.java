package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public UserService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<Users> findByidNumber(Long idNumber) {
        return userRepository.findByidNumber(idNumber);
    }

    @Transactional(readOnly = true)
    public List<Users> getAllUsers() {
        entityManager.clear();// clears hibernate cache
        return userRepository.findAll();
    }

    public Optional<Users> getUserById(Long idNumber) {
        return userRepository.findById(idNumber);
    }

    public void deleteUser(Long idNumber) {
        userRepository.deleteById(idNumber);
    }

    public List<Users> getAllAdmins() {
        return userRepository.findAllByRoles("ADMIN");
    }

    // find by user email
    public Optional<Users> findByEmail(String email){
        return userRepository.findByEmail(email);
      }

//    public Optional<Users> findByEmail(String email) {
//        return userRepository.findAll().stream()
//                .filter(user -> user.getEmail().equals(email))
//                .findFirst();
//    }
}