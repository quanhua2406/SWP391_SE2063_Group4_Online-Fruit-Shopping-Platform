package com.mycompany.onlinefruitshopping.service.commission;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs daily at 07:00: issues last month's commission settlements and
 * suspends shops whose settlement is more than one day overdue.
 */
@WebListener
public final class CommissionSettlementScheduler implements ServletContextListener {

    private static final int RUN_HOUR = 7;

    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "commission-settlement");
            thread.setDaemon(true);
            return thread;
        });

        executor.scheduleAtFixedRate(
                CommissionSettlementScheduler::runOnce,
                minutesUntilNextRun(),
                24 * 60,
                TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    static long minutesUntilNextRun() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().atTime(RUN_HOUR, 0);

        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }

        return Duration.between(now, next).toMinutes() + 1;
    }

    static void runOnce() {

        LocalDate today = LocalDate.now();
        CommissionSettlementService service = new CommissionSettlementService();

        try {
            service.issueSettlements(YearMonth.from(today).minusMonths(1), today);
            service.suspendOverdueShops(today);
        } catch (Exception e) {
            System.out.println("COMMISSION SETTLEMENT FAILED: " + e.getMessage());
        }
    }
}
