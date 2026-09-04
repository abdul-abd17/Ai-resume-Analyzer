package com.airesume.analyzer.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public HealthIndicator resuMatchEngineHealthIndicator() {
        return () -> Health.up()
                .withDetail("atsEngine", "Operational - High Throughput")
                .withDetail("grammarEngine", "Operational - LanguageTool v6.4")
                .withDetail("aiEngine", "Operational - Multi-Provider Subsystem")
                .withDetail("pdfEngine", "Operational - OpenPDF Generator")
                .build();
    }
}
