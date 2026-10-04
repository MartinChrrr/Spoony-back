package com.spoony.backend.infrastructure.config;

import com.spoony.backend.domain.energy.port.in.EnergyUseCase;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.energy.service.EnergyService;
import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.task.port.in.TaskUseCase;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.domain.task.service.TaskService;
import com.spoony.backend.domain.tasklog.port.in.TaskLogUseCase;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.domain.tasklog.service.TaskLogService;
import com.spoony.backend.domain.suggestion.port.in.SuggestionUseCase;
import com.spoony.backend.domain.suggestion.port.out.SuggestionPort;
import com.spoony.backend.domain.suggestion.service.SuggestionService;
import com.spoony.backend.domain.suggestion.strategy.DefaultSuggestionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableScheduling
public class BeanConfig {

    @Bean
    public Clock businessClock(@Value("${app.business-time-zone:Europe/Paris}") String businessTimeZone) {
        return Clock.system(ZoneId.of(businessTimeZone));
    }

    @Bean
    public TaskUseCase taskUseCase(TaskPort taskPort, Clock businessClock) {
        return new TaskService(taskPort, businessClock);
    }

    @Bean
    public EnergyUseCase energyUseCase(EnergyPort energyPort, TaskPostponePort taskPostponePort, Clock businessClock) {
        return new EnergyService(energyPort, taskPostponePort, businessClock);
    }

    @Bean
    public TaskLogUseCase taskLogUseCase(TaskLogPort taskLogPort, EnergyPort energyPort,
                                         TaskPostponePort taskPostponePort, Clock businessClock) {
        return new TaskLogService(taskLogPort, energyPort, taskPostponePort, businessClock);
    }

    @Bean
    public SuggestionUseCase suggestionUseCase(TaskPort taskPort, EnergyPort energyPort,
                                               SuggestionPort suggestionPort, TaskLogPort taskLogPort,
                                               Clock businessClock) {
        return new SuggestionService(taskPort, energyPort, suggestionPort, taskLogPort,
                new DefaultSuggestionStrategy(businessClock), businessClock);
    }
}
