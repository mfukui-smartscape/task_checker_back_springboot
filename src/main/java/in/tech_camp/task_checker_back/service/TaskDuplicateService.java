package in.tech_camp.task_checker_back.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.tech_camp.task_checker_back.entity.TaskEntity;
import in.tech_camp.task_checker_back.repository.TaskRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TaskDuplicateService {

    private final TaskRepository taskRepository;

    public ServiceResult<TaskEntity> duplicate(Integer taskId) {
        TaskEntity original = taskRepository.findById(taskId);
        if (original == null) {
            return ServiceResult.failure(List.of("Task not found"));
        }

        TaskEntity duplicated = new TaskEntity();
        duplicated.setName(original.getName() + "(コピー)");
        duplicated.setExplanation(original.getExplanation());
        duplicated.setDeadlineDate(original.getDeadlineDate());
        duplicated.setGenreId(original.getGenreId());
        duplicated.setPriority(original.getPriority());
        duplicated.setStatus(0);

        try {
            taskRepository.insert(duplicated);
        } catch (Exception e) {
            System.out.println("エラー：" + e);
            return ServiceResult.failure(List.of("Internal Server Error"));
        }

        return ServiceResult.success(duplicated);
    }
}
