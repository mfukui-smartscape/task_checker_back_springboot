package in.tech_camp.task_checker_back.service;

import java.util.List;

public record ServiceResult<T>(T data, List<String> errors) {

    public static <T> ServiceResult<T> success(T data) {
        return new ServiceResult<>(data, List.of());
    }

    public static <T> ServiceResult<T> failure(List<String> errors) {
        return new ServiceResult<>(null, errors);
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
