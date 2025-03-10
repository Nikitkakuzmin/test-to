package kz.nik.testto.service;

import kz.nik.testto.model.Comment;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.User;
import kz.nik.testto.repository.CommentRepository;
import kz.nik.testto.repository.TaskRepository;
import kz.nik.testto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Comment addComment(Long taskId, String email, String content) {
        log.info("Adding comment to task {} by user {}", taskId, email);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found: {}", taskId);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена");
                });

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });

        Comment comment = new Comment(null, task, author, content, LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        log.info("Comment added successfully by {} to task {}", email, taskId);
        return savedComment;
    }

    public List<Comment> getCommentsByTask(Long taskId) {
        log.info("Fetching comments for task {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            log.warn("Task not found: {}", taskId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена");
        }

        return commentRepository.findByTaskId(taskId);
    }
}
