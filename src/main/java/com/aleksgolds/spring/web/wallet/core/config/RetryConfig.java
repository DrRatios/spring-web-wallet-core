package com.aleksgolds.spring.web.wallet.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableRetry(order = Ordered.HIGHEST_PRECEDENCE)  // Обеспечиваем обработку Retry до Transactional
@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE)
public class RetryConfig {
}
