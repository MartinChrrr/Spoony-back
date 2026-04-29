package com.spoony.backend.domain.shared.port.out;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskPostponePort {

    int postponeAllActiveTasks(UUID userId, LocalDate fromDate, LocalDate toDate);
}
