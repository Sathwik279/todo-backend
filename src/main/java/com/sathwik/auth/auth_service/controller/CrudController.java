package com.sathwik.auth.auth_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final TodoService todoService;
    private final AiService aiService;
    private final SseService sseService;


    public CrudController(TodoService todoService,TodoRepository todoRepo, UserRepository userRepo,AiService aiService, SseService sseService){
        this.todoRepo = todoRepo;
        this.userRepo = userRepo;
        this.todoService = todoService;
        this.aiService = aiService;
        this.sseService = sseService;
    }

    @GetMapping(value="/stream", produces="text/event-stream")
    public SseEmitter streamEvents(Authentication authentication){
        String userId = authentication.getName();
        return sseService.subscribe(userId);
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
        // THE FIX
        TodoEntity newTodo = new TodoEntity(user, dto.getTitle(), dto.getDescription());
        TodoEntity savedTodo = todoRepo.save(newTodo); // Capture the returned object!
        return ResponseEntity.status(201).body(savedTodo); // Return the saved object
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

    public static boolean calcuateFetchAi(boolean titleChanged,boolean descriptionChanged,String currentAiContent){
        if(titleChanged||descriptionChanged||currentAiContent.equals(""))return true;
        return false;
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
                    System.out.println(dto.getAiContent());

                    // 🔹 Check changes BEFORE updating
                    boolean titleChanged = !todo.getTitle().equals(dto.getTitle());
                    boolean descriptionChanged =
                            !Objects.equals(todo.getDescription(), dto.getDescription());
                    // 1. UPDATE THE FIELDS IN MEMORY FIRST (You accidentally deleted these 4 lines!)
                    todo.setTitle(dto.getTitle());
                    todo.setDescription(dto.getDescription());
                    todo.setDone(dto.isDone());
                    todo.setAiEnabled(dto.isAiEnabled());

                    // 2. Save the user's text to the database FAST
                    TodoEntity savedTodo = todoRepo.save(todo);

                    // 3. FIRE AND FORGET: If AI is needed, hand it to the background thread
                    if (dto.isAiEnabled() && calcuateFetchAi(titleChanged, descriptionChanged, dto.getAiContent())) {
                        aiService.generateAndSaveAiContent(savedTodo.getId(), dto.getDescription(),userId);
                    }

                    // 4. Return immediately! The UI stays lightning fast.
                    return ResponseEntity.ok(savedTodo);
                })
                .orElse(ResponseEntity.notFound().build());
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
