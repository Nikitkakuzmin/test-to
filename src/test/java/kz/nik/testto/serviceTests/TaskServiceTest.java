package kz.nik.testto.serviceTests;

import jakarta.persistence.EntityNotFoundException;
import kz.nik.testto.model.Role;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.TaskStatus;
import kz.nik.testto.model.User;
import kz.nik.testto.repository.TaskRepository;
import kz.nik.testto.repository.UserRepository;
import kz.nik.testto.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User author;
    private User assignee;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setEmail("author@example.com");
        author.setRoles(Set.of(Role.USER));

        assignee = new User();
        assignee.setEmail("assignee@example.com");
        assignee.setRoles(Set.of(Role.USER));

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setAuthor(author);
        task.setAssignee(assignee);
    }

    @Test
    void createTask_ShouldSaveTask() {
        when(taskRepository.save(task)).thenReturn(task);

        Task createdTask = taskService.createTask(task);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task foundTask = taskService.getTaskById(1L);

        assertNotNull(foundTask);
        assertEquals(1L, foundTask.getId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(1L));

        assertEquals("Задача не найдена", exception.getMessage());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void updateTask_ShouldUpdate_WhenAuthorized() {
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(task.getPriority());
        updatedTask.setAssignee(task.getAssignee());

        lenient().when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        lenient().when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));
        lenient().when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTask(1L, updatedTask, "author@example.com");

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());

        verify(taskRepository, times(1)).save(task);
    }



    @Test
    void updateTask_ShouldThrowException_WhenUserNotAuthorized() {
        User anotherUser = new User();
        anotherUser.setEmail("hacker@example.com");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("hacker@example.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(1L, task, "hacker@example.com"));

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDelete_WhenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        Exception exception = assertThrows(EntityNotFoundException.class, () -> taskService.deleteTask(1L));

        assertEquals("Задача не найдена", exception.getMessage());
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void getTasks_ShouldReturnPagedTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(pageable)).thenReturn(page);

        Page<Task> result = taskService.getTasks(null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taskRepository, times(1)).findAll(pageable);
    }
}