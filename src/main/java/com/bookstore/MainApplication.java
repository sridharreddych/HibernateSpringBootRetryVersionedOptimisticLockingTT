package com.bookstore;

import com.bookstore.service.InventoryService;
import com.vladmihalcea.concurrent.aop.OptimisticConcurrencyControlAspect;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MainApplication {
    
    private final InventoryService inventoryService;

    public MainApplication(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public OptimisticConcurrencyControlAspect
            optimisticConcurrencyControlAspect() {

        return new OptimisticConcurrencyControlAspect();
    }
            
    @Bean
    public ApplicationRunner init() {
        return args -> {

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(inventoryService);
            // Thread.sleep(2000); -> adding a sleep here will break the transactions concurrency
            executor.execute(inventoryService);

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
    }
}

/*
 * 
 * 
 * How To Retry Transaction Via TransactionTemplate After OptimisticLockException Exception (@Version)

Note: Optimistic locking via @Version works for detached entities as well.

Description: This is a Spring Boot application that simulates a scenario that leads to an optimistic locking exception. When such exception occurs, the application retry the corresponding transaction via db-util library developed by Vlad Mihalcea.

Key points:

in pom.xml, add the db-util dependency
configure the OptimisticConcurrencyControlAspect bean
rely on TransactionTemplate
 * 
 * 
 */
