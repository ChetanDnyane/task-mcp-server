package com.chetan.taskflow.mcp.tool;

import org.springframework.ai.mcp.annotation.McpMeta;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class TaskMcpTools {

    private final RestClient restClient;

    public TaskMcpTools(
            @Value("${task-service.base-url}")
            String taskServiceBaseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(taskServiceBaseUrl)
                .build();
    }

    // =========================================================
    // GET ALL TASKS
    // =========================================================

    @McpTool(
            name = "getTasks",
            description = """
                    Get all TaskFlow tasks belonging to the
                    currently authenticated user.

                    Use this when the user asks to list, show,
                    view, summarize, prioritize or reason about
                    their tasks.
                    """
    )
    public List<TaskInfo> getTasks(
            McpMeta meta) {

        String authorizationHeader =
                getAuthorizationHeader(meta);

        TaskInfo[] tasks =
                restClient
                        .get()
                        .uri("/api/tasks")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeader
                        )
                        .retrieve()
                        .body(TaskInfo[].class);

        return tasks == null
                ? List.of()
                : List.of(tasks);
    }

    // =========================================================
    // GET TASK BY ID
    // =========================================================

    @McpTool(
            name = "getTaskById",
            description = """
                    Get one TaskFlow task belonging to the
                    currently authenticated user using its
                    numeric task ID.

                    Use this when the user asks to view or
                    inspect a specific task.
                    """
    )
    public TaskInfo getTaskById(
            Long taskId,
            McpMeta meta) {

        String authorizationHeader =
                getAuthorizationHeader(meta);

        return restClient
                .get()
                .uri(
                        "/api/tasks/{taskId}",
                        taskId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(TaskInfo.class);
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    @McpTool(
            name = "createTask",
            description = """
                    Create a new TaskFlow task for the currently
                    authenticated user.

                    Use this when the user explicitly asks to
                    create, add or save a new task.

                    Valid priorities are LOW, MEDIUM and HIGH.
                    dueDate should use ISO format YYYY-MM-DD
                    when supplied.
                    """
    )
    public TaskInfo createTask(
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            McpMeta meta) {

        String authorizationHeader =
                getAuthorizationHeader(meta);

        CreateTaskRequest request =
                new CreateTaskRequest(
                        title,
                        description,
                        priority,
                        dueDate
                );

        return restClient
                .post()
                .uri("/api/tasks")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TaskInfo.class);
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @McpTool(
            name = "updateTask",
            description = """
                    Update an existing TaskFlow task belonging
                    to the currently authenticated user.

                    Use this when the user explicitly asks to
                    modify a task.

                    Valid statuses are TODO, IN_PROGRESS and
                    COMPLETED.

                    Valid priorities are LOW, MEDIUM and HIGH.

                    dueDate should use ISO format YYYY-MM-DD
                    when supplied.
                    """
    )
    public TaskInfo updateTask(
            Long taskId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            McpMeta meta) {

        String authorizationHeader =
                getAuthorizationHeader(meta);

        UpdateTaskRequest request =
                new UpdateTaskRequest(
                        title,
                        description,
                        status,
                        priority,
                        dueDate
                );

        return restClient
                .put()
                .uri(
                        "/api/tasks/{taskId}",
                        taskId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TaskInfo.class);
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    @McpTool(
            name = "deleteTask",
            description = """
                    Delete an existing TaskFlow task belonging
                    to the currently authenticated user.

                    Use this only when the user explicitly asks
                    to delete or remove a specific task.
                    """
    )
    public String deleteTask(
            Long taskId,
            McpMeta meta) {

        String authorizationHeader =
                getAuthorizationHeader(meta);

        restClient
                .delete()
                .uri(
                        "/api/tasks/{taskId}",
                        taskId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();

        return "Task "
                + taskId
                + " deleted successfully.";
    }

    // =========================================================
    // SECURITY
    // =========================================================

    /*
     * The JWT comes from MCP metadata.
     *
     * It is NOT an MCP tool argument and therefore
     * is not controlled by the LLM.
     */
    private String getAuthorizationHeader(
            McpMeta meta) {

        String authorizationHeader =
                (String) meta.get(
                        "authorization"
                );

        if (authorizationHeader == null
                || !authorizationHeader
                .startsWith("Bearer ")) {

            throw new IllegalStateException(
                    "Authenticated user context is missing"
            );
        }

        return authorizationHeader;
    }

    // =========================================================
    // DTOs
    // =========================================================

    /*
     * Matches Task Service TaskResponse.
     */
    public record TaskInfo(
            Long id,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    /*
     * Matches Task Service CreateTaskRequest.
     */
    public record CreateTaskRequest(
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate
    ) {
    }

    /*
     * Matches Task Service UpdateTaskRequest.
     */
    public record UpdateTaskRequest(
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
    }

    /*
     * Mirrors Task Service TaskStatus.
     */
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        COMPLETED
    }

    /*
     * Mirrors Task Service TaskPriority.
     */
    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}