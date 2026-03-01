package com.sathwik.auth.auth_service.controller;

import com.sathwik.auth.auth_service.dto.AiRequest;
import com.sathwik.auth.auth_service.dto.TodoCreateRequest;
import com.sathwik.auth.auth_service.entity.TodoEntity;
import com.sathwik.auth.auth_service.entity.UserEntity;
import com.sathwik.auth.auth_service.repository.TodoRepository;
import com.sathwik.auth.auth_service.repository.UserRepository;
import com.sathwik.auth.auth_service.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crud")
public class CrudController {

    private final TodoRepository todoRepo;
    private final UserRepository userRepo;

    public CrudController(TodoRepository todoRepo,UserRepository userRepo){
        this.todoRepo = todoRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/todos")
    public List<TodoEntity> getTodos(Authentication authentication) {

        String userId = (String) authentication.getPrincipal(); // from JWT filter

        return todoRepo.findByUser_UserId(userId);
    }




    @PostMapping("/todo")
    public ResponseEntity<?> createTodo(@RequestBody TodoCreateRequest dto,
                                        Authentication authentication) {

        String userId = authentication.getName(); // principal = userId

        UserEntity user = userRepo.findById(userId).orElseThrow();

        todoRepo.save(new TodoEntity(user, dto.getTitle(), dto.getDescription()));

        return ResponseEntity.status(201).body(Map.of("msg", "Todo created"));
    }

    // Add this method inside your CrudController class

    @GetMapping("/todo/{id}")
    public ResponseEntity<?> getTodoById(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        // Find the todo by its ID and the logged-in user's ID
        return todoRepo.findByIdAndUser_UserId(id, userId)
                .map(todo -> ResponseEntity.ok(todo)) // If found, return it with a 200 OK
                .orElseGet(() ->
                        // Otherwise, return a 404 Not Found
                        ResponseEntity.status(404).body(null)
                );
    }

    @PutMapping("/todo/{id}")
    public ResponseEntity<TodoEntity> updateTodo(
            @PathVariable String id,
            @RequestBody TodoEntity dto,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return todoRepo.findByIdAndUser_UserId(id, userId)
                .map(todo -> {
                    todo.setTitle(dto.getTitle());
                    todo.setDescription(dto.getDescription());
                    todo.setDone(dto.isDone());
                    todo.setAiEnabled(dto.isAiEnabled());
                    todo.setAiContent(dto.getAiContent());

                    TodoEntity updatedTodo = todoRepo.save(todo);

                    return ResponseEntity.ok(updatedTodo);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/todo/{id}")
    public ResponseEntity<?> deleteTodo(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return todoRepo.findByIdAndUser_UserId(id, userId)
                .map(todo -> {
                    todoRepo.delete(todo);
                    return ResponseEntity.ok(Map.of("msg", "Todo deleted"));
                })
                .orElseGet(() ->
                        ResponseEntity.status(404)
                                .body(Map.of("error", "Todo not found"))
                );

    }
}
