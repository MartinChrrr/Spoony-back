package com.spoony.backend.domain.energy.port.out;

import java.time.LocalDate;
import java.util.UUID;

public interface BulkPostponePort {

    int postponeAllActiveTasks(UUID userId, LocalDate fromDate, LocalDate toDate);
}
