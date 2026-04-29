package com.spoony.backend.application.rest.tasklog;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.application.tasklog.TaskLogApplicationService;
import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/task-logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "TaskLogs", description = "Gestion des logs de tâches quotidiens")
public class TaskLogController {

    private final TaskLogApplicationService taskLogApplicationService;

    public TaskLogController(TaskLogApplicationService taskLogApplicationService) {
        this.taskLogApplicationService = taskLogApplicationService;
    }

    @GetMapping
    @Operation(summary = "Logs du jour", description = "Retourne les logs de tâches du jour. Par défaut exclut les complétés >24h.")
    @ApiResponse(responseCode = "200", description = "Logs retournés")
    public ResponseEntity<JSendResponse<List<TaskLogResponse>>> getTodayLogs(
            @RequestParam(name = "include_archived", defaultValue = "false") boolean includeArchived) {
        UUID userId = getCurrentUserId();
        List<TaskLogResponse> logs = taskLogApplicationService.getTodayLogs(userId, includeArchived).stream()
                .map(TaskLogResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(JSendResponse.success(logs));
    }

    @PostMapping
    @Operation(summary = "Créer des logs", description = "Crée des logs de tâches en bulk avec status PLANNED")
    @ApiResponse(responseCode = "201", description = "Logs créés")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<List<TaskLogResponse>>> createLogs(
            @Valid @RequestBody CreateTaskLogsRequest request) {
        UUID userId = getCurrentUserId();
        List<TaskLogResponse> logs = taskLogApplicationService.createLogs(request.getUserTaskIds(), userId).stream()
                .map(TaskLogResponse::fromDomain)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(JSendResponse.success(logs));
    }

    @PostMapping("/manual")
    @Operation(summary = "Créer un log manuel", description = "Crée un log unique avec suggested=false")
    @ApiResponse(responseCode = "201", description = "Log créé")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<TaskLogResponse>> createManualLog(
            @Valid @RequestBody CreateManualTaskLogRequest request) {
        UUID userId = getCurrentUserId();
        UserTaskLog log = taskLogApplicationService.createManualLog(request.getUserTaskId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(JSendResponse.success(TaskLogResponse.fromDomain(log)));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Changer le statut d'un log",
            description = "Met à jour le statut. Si COMPLETED : incrémente spoonsUsed. Si sort de COMPLETED : décrémente. Règle J+1."
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "403", description = "Log expiré (règle J+1)")
    @ApiResponse(responseCode = "404", description = "Log non trouvé")
    public ResponseEntity<JSendResponse<TaskLogResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskLogStatusRequest request) {
        UUID userId = getCurrentUserId();
        TaskLogStatus newStatus = TaskLogStatus.valueOf(request.getStatus());
        UserTaskLog log = taskLogApplicationService.updateStatus(id, newStatus, userId);
        return ResponseEntity.ok(JSendResponse.success(TaskLogResponse.fromDomain(log)));
    }

    @PostMapping("/bulk-postpone")
    @Operation(
            summary = "Reporter toutes les tâches PLANNED",
            description = "Reporte toutes les tâches PLANNED du jour au lendemain ou à la date cible"
    )
    @ApiResponse(responseCode = "200", description = "Tâches reportées")
    @ApiResponse(responseCode = "404", description = "Aucune tâche à reporter")
    public ResponseEntity<JSendResponse<BulkPostponeResponse>> bulkPostpone(
            @RequestBody(required = false) BulkPostponeRequest request) {
        UUID userId = getCurrentUserId();
        java.time.LocalDate targetDate = request != null ? request.getTargetDate() : null;
        BulkPostponeResult result = taskLogApplicationService.bulkPostpone(userId, targetDate);
        return ResponseEntity.ok(JSendResponse.success(BulkPostponeResponse.fromDomain(result)));
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
