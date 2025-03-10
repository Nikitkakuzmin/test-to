package kz.nik.testto.controllerTests;



import kz.nik.testto.controller.CommentController;
import kz.nik.testto.dto.CommentRequest;
import kz.nik.testto.model.Comment;
import kz.nik.testto.model.Role;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.User;
import kz.nik.testto.service.CommentService;
import kz.nik.testto.service.JwtService;
import kz.nik.testto.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc


public class CommentControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setRoles(Set.of(Role.USER));

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setAuthor(testUser);
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void addComment_ShouldReturnCreatedComment() throws Exception {
        Long taskId = testTask.getId();
        String testContent = "This is a test comment";

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent(testContent);

        Comment mockComment = new Comment(1L, testTask, testUser, testContent, LocalDateTime.now());

        when(commentService.addComment(eq(taskId), eq("testuser@example.com"), eq(testContent)))
                .thenReturn(mockComment);

        String requestJson = """
        {
            "content": "This is a test comment"
        }
    """;

        mockMvc.perform(post("/api/comments/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(testContent));
    }


    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void addComment_ShouldReturnBadRequest_WhenContentIsEmpty() throws Exception {
        Long taskId = testTask.getId();

        String requestJson = """
        {
            "content": ""
        }
    """;

        mockMvc.perform(post("/api/comments/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void getComments_ShouldReturnListOfComments() throws Exception {
        Long taskId = testTask.getId();

        List<Comment> comments = List.of(
                new Comment(1L, testTask, testUser, "First comment", LocalDateTime.now()),
                new Comment(2L, testTask, testUser, "Second comment", LocalDateTime.now())
        );

        when(commentService.getCommentsByTask(taskId)).thenReturn(comments);

        mockMvc.perform(get("/api/comments/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(comments.size()))
                .andExpect(jsonPath("$[0].content").value("First comment"))
                .andExpect(jsonPath("$[1].content").value("Second comment"));
    }

}
