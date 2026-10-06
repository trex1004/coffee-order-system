package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PointChargeLockTimeoutTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PointHistoryRepository pointHistoryRepository;
    @Autowired
    PlatformTransactionManager transactionManager;
    @LocalServerPort
    int port;

    private Long userId;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userId = userRepository.save(User.of("tester@test.com", "테스터")).getId();
    }

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void 락_대기가_3초를_넘기면_503을_돌려준다() throws Exception {
        // given: 다른 스레드가 users 행을 4초간 잠근다
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch locked = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> holder = executor.submit(() -> tx.execute(status -> {
            userRepository.findByIdForUpdate(userId);
            locked.countDown();
            sleepQuietly(4_000);
            return null;
        }));
        locked.await();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/points/charge"))
                        .header("Content-Type", "application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"userId\":" + userId + ",\"amount\":1000}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        // then
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(response.body()).contains("LOCK_TIMEOUT");

        holder.get();
        executor.shutdown();
    }


}