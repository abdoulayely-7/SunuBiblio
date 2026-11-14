package com.sunubiblio.emprunts.internal.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class ClockConfiguration {

    @Bean
    Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
