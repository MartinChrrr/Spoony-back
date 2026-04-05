package com.spoony.backend.application.rest.task;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.domain.task.model.TaskFromCatalogCommand;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.in.TaskUseCase;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tasks", description = "Gestion des tâches utilisateur")
public class TaskController {

    private final TaskUseCase taskUseCase;

    public TaskController(TaskUseCase taskUseCase) {
        this.taskUseCase = taskUseCase;
    }

    @GetMapping
    @Operation(summary = "Lister les tâches actives", description = "Retourne toutes les tâches actives de l'utilisateur connecté")
    @ApiResponse(responseCode = "200", description = "Liste des tâches actives")
    public ResponseEntity<JSendResponse<List<TaskResponse>>> findAll() {
        UUID userId = getCurrentUserId();
        List<TaskResponse> tasks = taskUseCase.findAllActiveByUserId(userId).stream()
                .map(TaskResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(JSendResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une tâche", description = "Retourne une tâche par son identifiant")
    @ApiResponse(responseCode = "200", description = "Tâche trouvée")
    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    public ResponseEntity<JSendResponse<TaskResponse>> findById(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        UserTask task = taskUseCase.findByIdAndUserId(id, userId)
                .orElseThrow(TaskNotFoundException::new);
        return ResponseEntity.ok(JSendResponse.success(TaskResponse.fromDomain(task)));
    }

    @PostMapping
    @Operation(
            summary = "Créer une tâche",
            description = "Création rapide : seul le nom est requis. Les défauts sont spoonCost=2, importance=MEDIUM, dueDate=aujourd'hui"
    )
    @ApiResponse(responseCode = "201", description = "Tâche créée")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        UUID userId = getCurrentUserId();
        UserTask created = taskUseCase.create(request.toDomain(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(JSendResponse.success(TaskResponse.fromDomain(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une tâche", description = "Met à jour les champs fournis d'une tâche existante")
    @ApiResponse(responseCode = "200", description = "Tâche mise à jour")
    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<TaskResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        UUID userId = getCurrentUserId();
        UserTask updated = taskUseCase.update(id, request.toDomain(), userId);
        return ResponseEntity.ok(JSendResponse.success(TaskResponse.fromDomain(updated)));
    }

    @PostMapping("/from-catalog")
    @Operation(summary = "Créer des tâches depuis le catalogue", description = "Crée des tâches utilisateur à partir de tâches prédéfinies du catalogue")
    @ApiResponse(responseCode = "201", description = "Tâches créées")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    @ApiResponse(responseCode = "404", description = "Tâche de base non trouvée")
    public ResponseEntity<JSendResponse<List<TaskResponse>>> createFromCatalog(
            @Valid @RequestBody CreateTasksFromCatalogRequest request) {
        UUID userId = getCurrentUserId();
        List<TaskFromCatalogCommand> commands = request.getTasks().stream()
                .map(item -> new TaskFromCatalogCommand(item.getBaseTaskId(), item.getName()))
                .toList();
        List<TaskResponse> tasks = taskUseCase.createFromCatalog(commands, userId).stream()
                .map(TaskResponse::fromDomain)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(JSendResponse.success(tasks));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une tâche", description = "Supprime une tâche de l'utilisateur")
    @ApiResponse(responseCode = "204", description = "Tâche supprimée")
    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        taskUseCase.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
