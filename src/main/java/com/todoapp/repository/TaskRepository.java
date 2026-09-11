package com.todoapp.repository;

import com.todoapp.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Task> findByIdAndUserId(String id, String userId);
}
