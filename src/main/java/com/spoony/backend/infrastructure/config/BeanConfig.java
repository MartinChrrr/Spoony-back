package com.spoony.backend.infrastructure.config;

import com.spoony.backend.domain.task.port.in.TaskUseCase;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.domain.task.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public TaskUseCase taskUseCase(TaskPort taskPort) {
        return new TaskService(taskPort);
    }
}
