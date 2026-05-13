package in.tech_camp.task_checker_back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import in.tech_camp.task_checker_back.entity.TaskEntity;
import in.tech_camp.task_checker_back.repository.TaskRepository;

@SpringBootTest
@DisplayName("TaskDuplicateService")
class TaskDuplicateServiceTest {

    @Autowired
    private TaskDuplicateService taskDuplicateService;

    @MockitoBean
    private TaskRepository taskRepository;

    private TaskEntity baseTask;

    @BeforeEach
    void setUpBaseTask() {
        baseTask = new TaskEntity();
        baseTask.setId(1);
        baseTask.setName("要件定義");
        baseTask.setExplanation("クライアントにヒアリングして要件を決定します");
        baseTask.setStatus(5);
        baseTask.setGenreId(1);
        baseTask.setPriority("high");
    }

    private void mockInsertWithGeneratedId(int generatedId) {
        doAnswer(invocation -> {
            TaskEntity t = invocation.getArgument(0);
            t.setId(generatedId);
            return null;
        }).when(taskRepository).insert(any(TaskEntity.class));
    }

    // -----------------------------------------------------------------------
    // A-1. 正常系 - 基本複製
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("A-1. 正常系 - 基本複製")
    class BasicDuplication {

        @BeforeEach
        void setUp() {
            when(taskRepository.findById(1)).thenReturn(baseTask);
            mockInsertWithGeneratedId(99);
        }

        @Test
        @DisplayName("A-1-1: 存在するタスクを複製できること")
        void duplicateSucceeds() {
            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("A-1-2: 複製されたタスクに新しいIDが払い出されること")
        void duplicatedTaskHasNewId() {
            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getId()).isNotEqualTo(baseTask.getId());
        }

        @Test
        @DisplayName("A-1-3: explanation がそのまま引き継がれること")
        void explanationIsCopied() {
            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getExplanation()).isEqualTo(baseTask.getExplanation());
        }

        @Test
        @DisplayName("A-1-4: genreId がそのまま引き継がれること")
        void genreIdIsCopied() {
            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getGenreId()).isEqualTo(baseTask.getGenreId());
        }

        @Test
        @DisplayName("A-1-5: priority がそのまま引き継がれること")
        void priorityIsCopied() {
            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getPriority()).isEqualTo(baseTask.getPriority());
        }
    }

    // -----------------------------------------------------------------------
    // A-2. 正常系 - name の加工（末尾に「(コピー)」追加）
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("A-2. 正常系 - name の加工")
    class NameProcessing {

        @BeforeEach
        void setUp() {
            doAnswer(invocation -> null).when(taskRepository).insert(any(TaskEntity.class));
        }

        @Test
        @DisplayName("A-2-1: name の末尾に「(コピー)」が追加されること")
        void appendsCopyLabel() {
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getName()).isEqualTo("要件定義(コピー)");
        }

        @Test
        @DisplayName("A-2-2: name が空文字のとき「(コピー)」のみになること")
        void emptyNameBecomesLabelOnly() {
            baseTask.setName("");
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getName()).isEqualTo("(コピー)");
        }

        @Test
        @DisplayName("A-2-3: name が既に「(コピー)」で終わるとき、さらに「(コピー)」が追加されること")
        void appendsLabelAgainIfAlreadyPresent() {
            baseTask.setName("要件定義(コピー)");
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getName()).isEqualTo("要件定義(コピー)(コピー)");
        }

        @Test
        @Disabled("仕様確認待ち: name が VARCHAR(255) 上限付近でオーバーフローする場合の切り捨てポリシーが未定義")
        @DisplayName("A-2-4: name が251文字のとき「(コピー)」追加後に VARCHAR(255) を超える場合の動作")
        void longNameExceedsDbLimit() {
            String longName = "あ".repeat(251); // 251文字 + "(コピー)" 5文字 = 256文字
            baseTask.setName(longName);
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            // 仕様確定後にアサーションを追加すること
            assertThat(result.data().getName().length()).isLessThanOrEqualTo(255);
        }
    }

    // -----------------------------------------------------------------------
    // A-3. 正常系 - status の初期化
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("A-3. 正常系 - status の初期化")
    class StatusInitialization {

        @BeforeEach
        void setUp() {
            doAnswer(invocation -> null).when(taskRepository).insert(any(TaskEntity.class));
        }

        @Test
        @DisplayName("A-3-1: 元の status が 0 でも複製後は 0 になること")
        void statusIsZeroWhenOriginalIsZero() {
            baseTask.setStatus(0);
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("A-3-2: 元の status が 1〜4（進行中）でも複製後は 0 になること")
        void statusIsZeroWhenOriginalIsInProgress() {
            for (int status : List.of(1, 2, 3, 4)) {
                baseTask.setStatus(status);
                when(taskRepository.findById(1)).thenReturn(baseTask);

                ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

                assertThat(result.data().getStatus())
                    .as("元の status=%d でも複製後は 0 であること", status)
                    .isEqualTo(0);
            }
        }

        @Test
        @DisplayName("A-3-3: 元の status が 5（完了）でも複製後は 0 になること")
        void statusIsZeroWhenOriginalIsCompleted() {
            baseTask.setStatus(5);
            when(taskRepository.findById(1)).thenReturn(baseTask);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getStatus()).isEqualTo(0);
        }
    }

    // -----------------------------------------------------------------------
    // A-4. 正常系 - priority の引き継ぎ
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("A-4. 正常系 - priority の引き継ぎ")
    class PriorityInheritance {

        @BeforeEach
        void setUp() {
            when(taskRepository.findById(1)).thenReturn(baseTask);
            doAnswer(invocation -> null).when(taskRepository).insert(any(TaskEntity.class));
        }

        @Test
        @DisplayName("A-4-1: priority \"high\" が引き継がれること")
        void highPriorityIsCopied() {
            baseTask.setPriority("high");

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getPriority()).isEqualTo("high");
        }

        @Test
        @DisplayName("A-4-2: priority \"medium\" が引き継がれること")
        void mediumPriorityIsCopied() {
            baseTask.setPriority("medium");

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getPriority()).isEqualTo("medium");
        }

        @Test
        @DisplayName("A-4-3: priority \"low\" が引き継がれること")
        void lowPriorityIsCopied() {
            baseTask.setPriority("low");

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.data().getPriority()).isEqualTo("low");
        }
    }

    // -----------------------------------------------------------------------
    // A-5. 異常系
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("A-5. 異常系")
    class ErrorCases {

        @Test
        @DisplayName("A-5-1: 存在しない ID を指定したとき isSuccess() が false になること")
        void returnsFailureForNonExistentId() {
            when(taskRepository.findById(999)).thenReturn(null);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(999);

            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("A-5-2: 存在しない ID を指定したときエラーメッセージが返ること")
        void returnsNotFoundMessageForNonExistentId() {
            when(taskRepository.findById(999)).thenReturn(null);

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(999);

            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("A-5-3: DB 保存時に例外が発生した場合、失敗の ServiceResult が返ること")
        void returnsFailureWhenDbThrowsException() {
            when(taskRepository.findById(1)).thenReturn(baseTask);
            doThrow(new RuntimeException("DB error"))
                .when(taskRepository).insert(any(TaskEntity.class));

            ServiceResult<TaskEntity> result = taskDuplicateService.duplicate(1);

            assertThat(result.isSuccess()).isFalse();
        }
    }
}
