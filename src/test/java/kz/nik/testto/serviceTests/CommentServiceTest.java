package kz.nik.testto.serviceTests;

import kz.nik.testto.model.Comment;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.User;
import kz.nik.testto.repository.CommentRepository;
import kz.nik.testto.repository.TaskRepository;
import kz.nik.testto.repository.UserRepository;
import kz.nik.testto.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Task task;
    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        comment = new Comment(1L, task, user, "Test comment", LocalDateTime.now());
    }

    @Test
    void addComment_ShouldAddComment_WhenTaskAndUserExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment savedComment = commentService.addComment(1L, "test@example.com", "Test comment");

        assertNotNull(savedComment);
        assertEquals("Test comment", savedComment.getContent());
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(1L, "test@example.com", "Test comment")
        );

        assertEquals("404 NOT_FOUND \"Задача не найдена\"", exception.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void addComment_ShouldThrowException_WhenUserNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(1L, "test@example.com", "Test comment")
        );

        assertEquals("404 NOT_FOUND \"Пользователь не найден\"", exception.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getCommentsByTask_ShouldReturnComments_WhenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskId(1L)).thenReturn(List.of(comment));

        List<Comment> comments = commentService.getCommentsByTask(1L);

        assertNotNull(comments);
        assertEquals(1, comments.size());
        verify(taskRepository, times(1)).existsById(1L);
        verify(commentRepository, times(1)).findByTaskId(1L);
    }

    @Test
    void getCommentsByTask_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.getCommentsByTask(1L)
        );

        assertEquals("404 NOT_FOUND \"Задача не найдена\"", exception.getMessage());
        verify(taskRepository, times(1)).existsById(1L);
        verifyNoInteractions(commentRepository);
    }
}