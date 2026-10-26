package com.harsh.bank;

import com.harsh.bank.exception.InsufficientFundsException;
import com.harsh.bank.model.Account;
import com.harsh.bank.service.BankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrencyTest {
    @Autowired BankService bank;

    @Test
    void concurrentWithdrawalsNeverOverdraw() throws Exception {
        Account a = bank.create("Race Test", new BigDecimal("1000.00"));
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger(), rejected = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    bank.withdraw(a.getId(), new BigDecimal("200.00"));
                    ok.incrementAndGet();
                } catch (InsufficientFundsException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException ignored) {}
            });
        }
        ready.await();
        go.countDown();            // release all threads at the same moment
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(5, ok.get());          // 1000 / 200 = exactly 5 can succeed
        assertEquals(5, rejected.get());
        assertEquals(0, bank.get(a.getId()).getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void transferIsAtomicOnFailure() {
        Account from = bank.create("A", new BigDecimal("100.00"));
        Account to = bank.create("B", new BigDecimal("50.00"));
        assertThrows(InsufficientFundsException.class,
                () -> bank.transfer(from.getId(), to.getId(), new BigDecimal("500.00")));
        assertEquals(0, bank.get(from.getId()).getBalance().compareTo(new BigDecimal("100.00")));
        assertEquals(0, bank.get(to.getId()).getBalance().compareTo(new BigDecimal("50.00")));
    }

    @Test
    void oppositeTransfersDoNotDeadlock() throws Exception {
        Account a = bank.create("X", new BigDecimal("1000.00"));
        Account b = bank.create("Y", new BigDecimal("1000.00"));
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 50; i++) {
            pool.submit(() -> bank.transfer(a.getId(), b.getId(), new BigDecimal("1.00")));
            pool.submit(() -> bank.transfer(b.getId(), a.getId(), new BigDecimal("1.00")));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "Deadlock suspected");
        BigDecimal total = bank.get(a.getId()).getBalance().add(bank.get(b.getId()).getBalance());
        assertEquals(0, total.compareTo(new BigDecimal("2000.00")));   // money is conserved
    }
}
