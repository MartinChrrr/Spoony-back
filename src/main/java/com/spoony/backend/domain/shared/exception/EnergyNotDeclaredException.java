package com.spoony.backend.domain.shared.exception;

public class EnergyNotDeclaredException extends BusinessException {

    public EnergyNotDeclaredException() {
        super("ENERGY_NOT_DECLARED", "L'énergie du jour n'a pas encore été déclarée", 404);
    }
}
