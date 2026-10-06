package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.point.dto.ChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


@SpringBootTest
@ActiveProfiles("test")
class PointServiceTest {

    @Autowired
    PointService pointService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PointHistoryRepository pointHistoryRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userId = userRepository.save(User.of("tester@test.com", "테스터")).getId();
    }

//    @AfterEach
//    void tearDown() {
//        pointHistoryRepository.deleteAllInBatch();
//        userRepository.deleteAllInBatch();
//    }

    private List<Throwable> runConcurrently(int threadCount, IntConsumer task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 모든 스레드가 준비될 때까지 대기시킨 후 동시에 작업을 시작하기 위한 latch
        CountDownLatch start = new CountDownLatch(1);

        // 모든 작업이 완료될 때까지 테스트 스레드가 대기하기 위한 latch
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executor.submit(() -> {
                try {
                    start.await();
                    task.accept(index);
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();
        return failures;
    }

    @Test
    void 동시_충전_두_건의_응답_잔액이_겹치지_않는다() throws Exception {
        // given
        pointService.charge(new ChargeRequest(userId, 3_000L), UUID.randomUUID().toString());
        List<Long> balances = Collections.synchronizedList(new ArrayList<>());

        // when
        List<Throwable> failures = runConcurrently(2, i -> balances.add(
                pointService.charge(new ChargeRequest(userId, 1_000L), UUID.randomUUID().toString()).balance()));

        // then
        assertThat(failures).isEmpty();
        assertThat(balances).containsExactlyInAnyOrder(4_000L, 5_000L);
    }

    @Test
    void 동시_충전_100건_뒤_잔액이_정확하다() throws Exception {
        // given
        int threadCount = 100;
        long chargeAmount = 1_000L;

        // when
        List<Throwable> failures = runConcurrently(threadCount, i ->
                pointService.charge(new ChargeRequest(userId, chargeAmount), UUID.randomUUID().toString()));

        // then
        assertThat(failures).isEmpty();

        long balance = userRepository.findById(userId).orElseThrow().getPoint();
        long ledgerSum = pointHistoryRepository.findAll().stream()
                .mapToLong(PointHistory::getAmount)
                .sum();

        assertThat(balance).isEqualTo(threadCount * chargeAmount);
        assertThat(ledgerSum).isEqualTo(balance);
        assertThat(pointHistoryRepository.count()).isEqualTo(threadCount);
    }

    @Test
    void 같은_키로_두_번_충전해도_한_번만_반영된다() {
        // given
        String key = UUID.randomUUID().toString();
        ChargeRequest request = new ChargeRequest(userId, 1_000L);
        ChargeResponse first = pointService.charge(request, key);

        // when
        ChargeResponse second = pointService.charge(request, key);

        // then
        assertThat(second).isEqualTo(first);                   // 두 응답이 글자까지 같다
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(1_000L);
        assertThat(pointHistoryRepository.count()).isEqualTo(1L);
    }

    @Test
    void 같은_키에_다른_금액이면_거절한다() {
        // given
        String key = UUID.randomUUID().toString();
        pointService.charge(new ChargeRequest(userId, 1_000L), key);

        // when
        Throwable thrown = catchThrowable(() ->
                pointService.charge(new ChargeRequest(userId, 9_999L), key));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);

        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(1_000L);
        assertThat(pointHistoryRepository.count()).isEqualTo(1L);
    }

    @Test
    void 같은_키로_동시에_두_건을_보내도_한_번만_반영된다() throws Exception {
        // given
        String key = UUID.randomUUID().toString();
        List<ChargeResponse> responses = Collections.synchronizedList(new ArrayList<>());

        // when
        List<Throwable> failures = runConcurrently(2, i ->
                responses.add(pointService.charge(new ChargeRequest(userId, 5_000L), key)));

        // then
        assertThat(failures).isEmpty();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0)).isEqualTo(responses.get(1));
        assertThat(pointHistoryRepository.count()).isEqualTo(1L);
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(5_000L);
    }

}