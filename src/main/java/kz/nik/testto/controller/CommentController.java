package kz.nik.testto.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.nik.testto.dto.CommentRequest;
import kz.nik.testto.model.Comment;
import kz.nik.testto.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Комментарии", description = "API для работы с комментариями")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{taskId}")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentRequest request, // Теперь принимаем объект
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} is adding a comment to task {}", userDetails.getUsername(), taskId);

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Комментарий не может быть пустым");
        }

        Comment comment = commentService.addComment(taskId, userDetails.getUsername(), request.getContent());
        log.info("Comment added successfully to task {} by {}", taskId, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }


    @Operation(summary = "Получить комментарии к задаче", description = "Возвращает список комментариев для конкретной задачи.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Комментарии получены"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        log.info("Fetching comments for task {}", taskId);
        List<Comment> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }
}
