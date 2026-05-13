package in.tech_camp.task_checker_back.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class TaskEntity {
    private Integer id;
    private String name;
    private String explanation;
    private Timestamp deadlineDate;
    private Integer status;
    private Integer genreId;
    private String priority = "medium";

    @JsonIgnore
    private Timestamp createdAt;
    @JsonIgnore
    private Timestamp updatedAt;
}