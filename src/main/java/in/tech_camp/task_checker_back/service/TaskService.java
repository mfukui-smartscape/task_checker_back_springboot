package in.tech_camp.task_checker_back.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import in.tech_camp.task_checker_back.entity.GenreEntity;
import in.tech_camp.task_checker_back.entity.TaskEntity;
import in.tech_camp.task_checker_back.repository.GenreRepository;
import in.tech_camp.task_checker_back.repository.TaskRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TaskService {

    private static final List<String> VALID_PRIORITIES = List.of("low", "medium", "high");

    private final TaskRepository taskRepository;
    private final GenreRepository genreRepository;

    public ServiceResult<TaskEntity> create(TaskEntity task) {
        List<String> errors = validate(task);

        if (task.getGenreId() != null) {
            GenreEntity genre = genreRepository.findById(task.getGenreId());
            if (genre == null) {
                errors.add("指定されたジャンルが存在しません");
            }
        }

        if (!errors.isEmpty()) {
            return ServiceResult.failure(errors);
        }

        try {
            taskRepository.insert(task);
        } catch (Exception e) {
            System.out.println("エラー：" + e);
            return ServiceResult.failure(List.of("Internal Server Error"));
        }

        return ServiceResult.success(task);
    }

    private List<String> validate(TaskEntity task) {
        List<String> errors = new ArrayList<>();

        if (!VALID_PRIORITIES.contains(task.getPriority())) {
            errors.add("priority は low, medium, high のいずれかを指定してください");
        }

        return errors;
    }
}
