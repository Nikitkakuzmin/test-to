package kz.nik.testto.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.TaskStatus;
import kz.nik.testto.model.User;
import kz.nik.testto.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Задачи", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Создать новую задачу", description = "Создает новую задачу и возвращает ее данные.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, @AuthenticationPrincipal User user) {
        log.info("User {} is creating a new task: {}", user.getEmail(), task.getTitle());
        task.setAuthor(user);
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Operation(summary = "Получить задачу по ID", description = "Возвращает данные конкретной задачи.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        log.info("Fetching task with ID {}", id);
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Обновить задачу", description = "Позволяет изменить данные задачи.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача обновлена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} is updating task {}", userDetails.getUsername(), id);
        Task updatedTask = taskService.updateTask(id, task, userDetails.getUsername());
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Удалить задачу", description = "Удаляет задачу по ее ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Задача удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Deleting task with ID {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список задач", description = "Возвращает список задач с фильтрацией и пагинацией.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач получен")
    })
    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching tasks with filters - Author: {}, Assignee: {}, Status: {}", author, assignee, status);
        Page<Task> tasks = taskService.getTasks(author, assignee, status, page, size);
        return ResponseEntity.ok(tasks);
    }
}
