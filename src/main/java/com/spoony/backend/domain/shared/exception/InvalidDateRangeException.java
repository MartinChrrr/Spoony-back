package com.spoony.backend.domain.shared.exception;

public class InvalidDateRangeException extends BusinessException {

    public InvalidDateRangeException() {
        super("INVALID_DATE_RANGE",
                "La plage de dates doit contenir au maximum 366 jours et inclure une date de début antérieure à la date de fin.",
                400);
    }
}
