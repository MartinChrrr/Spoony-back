package com.spoony.backend.domain.shared.exception;

public class SpoonBalanceConflictException extends BusinessException {

    public SpoonBalanceConflictException() {
        super("SPOON_BALANCE_CONFLICT",
                "Le compteur de cuillères a changé. Rechargez les données puis réessayez.", 409);
    }
}
