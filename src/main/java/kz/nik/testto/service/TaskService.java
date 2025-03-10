package kz.nik.testto.service;

import jakarta.persistence.EntityNotFoundException;
import kz.nik.testto.model.Role;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.TaskStatus;
import kz.nik.testto.repository.TaskRepository;
import kz.nik.testto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    public Task createTask(Task task) {
        log.info("Creating a new task: {}", task.getTitle());
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        log.info("Fetching task by ID: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: {}", id);
                    return new EntityNotFoundException("Задача не найдена");
                });
    }

    public Task updateTask(Long id, Task updatedTask, String userEmail) {
        log.info("Updating task {} by user {}", id, userEmail);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: {}", id);
                    return new EntityNotFoundException("Задача не найдена");
                });

        if (updatedTask == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данные задачи не могут быть пустыми");
        }
        if (!task.getAuthor().getEmail().equals(userEmail) && !isAdmin(userEmail)) {
            log.warn("User {} is not authorized to update task {}", userEmail, id);
            throw new AccessDeniedException("У вас нет прав на редактирование этой задачи");
        }

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setPriority(updatedTask.getPriority());
        task.setAssignee(updatedTask.getAssignee());

        Task savedTask = taskRepository.save(task);
        log.info("Task {} updated successfully by {}", id, userEmail);
        return savedTask;
    }


    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    boolean isAdmin = user.getRoles().contains(Role.ADMIN);
                    log.info("User {} is admin: {}", email, isAdmin);
                    return isAdmin;
                })
                .orElse(false);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            log.warn("Task not found: {}", id);
            throw new EntityNotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }

    public Page<Task> getTasks(String author, String assignee, TaskStatus status, int page, int size) {
        log.info("Fetching tasks with filters - Author: {}, Assignee: {}, Status: {}", author, assignee, status);
        Pageable pageable = PageRequest.of(page, size);

        if (author != null) {
            return taskRepository.findByAuthorEmail(author, pageable);
        } else if (assignee != null) {
            return taskRepository.findByAssigneeEmail(assignee, pageable);
        } else if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        } else {
            return taskRepository.findAll(pageable);
        }
    }
}
