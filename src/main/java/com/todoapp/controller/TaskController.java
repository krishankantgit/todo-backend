package com.todoapp.controller;

import com.todoapp.dto.TaskRequest;
import com.todoapp.model.Task;
import com.todoapp.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(taskRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest req, Authentication auth) {
        String userId = auth.getName();
        Task task = new Task(userId, req.title(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRepository.save(task));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTaskCompletion(@PathVariable String id, Authentication auth) {
        String userId = auth.getName();
        return taskRepository.findByIdAndUserId(id, userId)
                .map(task -> {
                    task.setCompleted(!task.isCompleted());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id, Authentication auth) {
        String userId = auth.getName();
        return taskRepository.findByIdAndUserId(id, userId)
                .map(task -> {
                    taskRepository.delete(task);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
