package com.spoony.backend.domain.shared.exception;

public class EnergyAlreadyDeclaredException extends BusinessException {

    public EnergyAlreadyDeclaredException() {
        super("ENERGY_ALREADY_DECLARED", "L'énergie du jour a déjà été déclarée", 409);
    }
}
