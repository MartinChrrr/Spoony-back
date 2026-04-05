package com.spoony.backend.application.rest.energy;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.in.EnergyUseCase;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/energy", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Energy", description = "Gestion de l'énergie quotidienne (cuillères)")
public class EnergyController {

    private final EnergyUseCase energyUseCase;

    public EnergyController(EnergyUseCase energyUseCase) {
        this.energyUseCase = energyUseCase;
    }

    @GetMapping("/today")
    @Operation(summary = "Énergie du jour", description = "Retourne l'énergie déclarée pour aujourd'hui")
    @ApiResponse(responseCode = "200", description = "Énergie trouvée")
    @ApiResponse(responseCode = "404", description = "Énergie non déclarée")
    public ResponseEntity<JSendResponse<EnergyResponse>> getToday() {
        UUID userId = getCurrentUserId();
        DailyEnergy energy = energyUseCase.getTodayEnergy(userId);
        return ResponseEntity.ok(JSendResponse.success(EnergyResponse.fromDomain(energy)));
    }

    @PostMapping
    @Operation(
            summary = "Déclarer l'énergie du jour",
            description = "Déclare le nombre de cuillères pour aujourd'hui. Si 0, toutes les tâches actives sont reportées à demain."
    )
    @ApiResponse(responseCode = "201", description = "Énergie déclarée")
    @ApiResponse(responseCode = "409", description = "Énergie déjà déclarée")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<EnergyResponse>> declare(
            @Valid @RequestBody DeclareEnergyRequest request) {
        UUID userId = getCurrentUserId();
        DailyEnergy energy = energyUseCase.declareEnergy(request.getSpoons(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JSendResponse.success(EnergyResponse.fromDomain(energy)));
    }

    @PutMapping("/today")
    @Operation(
            summary = "Réévaluer les cuillères",
            description = "Met à jour le nombre de cuillères en cours de journée (réévaluation mi-journée)"
    )
    @ApiResponse(responseCode = "200", description = "Cuillères mises à jour")
    @ApiResponse(responseCode = "404", description = "Énergie non déclarée")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<EnergyResponse>> updateSpoons(
            @Valid @RequestBody UpdateSpoonsRequest request) {
        UUID userId = getCurrentUserId();
        DailyEnergy energy = energyUseCase.updateSpoons(request.getSpoons(), userId);
        return ResponseEntity.ok(JSendResponse.success(EnergyResponse.fromDomain(energy)));
    }

    @PatchMapping("/today/mood")
    @Operation(
            summary = "Enregistrer l'humeur de fin de journée",
            description = "Met à jour l'humeur de fin de journée"
    )
    @ApiResponse(responseCode = "200", description = "Humeur mise à jour")
    @ApiResponse(responseCode = "404", description = "Énergie non déclarée")
    @ApiResponse(responseCode = "400", description = "Erreur de validation")
    public ResponseEntity<JSendResponse<EnergyResponse>> updateMood(
            @Valid @RequestBody UpdateMoodRequest request) {
        UUID userId = getCurrentUserId();
        DailyEnergy energy = energyUseCase.updateMood(request.getMoodEnd(), userId);
        return ResponseEntity.ok(JSendResponse.success(EnergyResponse.fromDomain(energy)));
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
