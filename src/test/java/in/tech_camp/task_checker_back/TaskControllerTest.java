package in.tech_camp.task_checker_back;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import in.tech_camp.task_checker_back.controller.TaskController;
import in.tech_camp.task_checker_back.entity.TaskEntity;
import in.tech_camp.task_checker_back.repository.TaskRepository;
import in.tech_camp.task_checker_back.service.ServiceResult;
import in.tech_camp.task_checker_back.service.TaskDuplicateService;
import in.tech_camp.task_checker_back.service.TaskService;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskDuplicateService taskDuplicateService;

    @Nested
    @DisplayName("POST /api/tasks/ - 優先度（priority）機能")
    class CreateTaskWithPriority {

        @Test
        @DisplayName("優先度「high」を指定してタスクを作成できること")
        void createTaskWithHighPriority() throws Exception {
            TaskEntity savedTask = new TaskEntity();
            savedTask.setId(1);
            savedTask.setName("テストタスク");
            savedTask.setPriority("high");

            when(taskService.create(any())).thenReturn(ServiceResult.success(savedTask));
            when(taskRepository.findAll()).thenReturn(List.of(savedTask));

            mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"テストタスク\",\"priority\":\"high\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("high"));
        }

        @Test
        @DisplayName("優先度「low」を指定してタスクを作成できること")
        void createTaskWithLowPriority() throws Exception {
            TaskEntity savedTask = new TaskEntity();
            savedTask.setId(1);
            savedTask.setName("テストタスク");
            savedTask.setPriority("low");

            when(taskService.create(any())).thenReturn(ServiceResult.success(savedTask));
            when(taskRepository.findAll()).thenReturn(List.of(savedTask));

            mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"テストタスク\",\"priority\":\"low\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("low"));
        }

        @Test
        @DisplayName("優先度を指定しない場合、デフォルト値「medium」が設定されること")
        void createTaskWithDefaultPriority() throws Exception {
            when(taskService.create(any())).thenReturn(ServiceResult.success(new TaskEntity()));
            when(taskRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"テストタスク\"}"))
                .andExpect(status().isOk());

            ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
            verify(taskService).create(captor.capture());
            assertThat(captor.getValue().getPriority()).isEqualTo("medium");
        }

        @Test
        @DisplayName("タスク作成後のレスポンスJSONに priority フィールドが含まれること")
        void createTaskResponseContainsPriorityField() throws Exception {
            TaskEntity savedTask = new TaskEntity();
            savedTask.setId(1);
            savedTask.setName("テストタスク");
            savedTask.setPriority("medium");

            when(taskService.create(any())).thenReturn(ServiceResult.success(savedTask));
            when(taskRepository.findAll()).thenReturn(List.of(savedTask));

            mockMvc.perform(post("/api/tasks/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"テストタスク\",\"priority\":\"medium\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").exists());
        }
    }
}
