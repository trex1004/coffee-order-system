package com.example.coffeeordersystem.domain.order.facade;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.dto.OrderItemRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResult;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.example.coffeeordersystem.support.ConcurrencyTestSupport.runConcurrently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@ActiveProfiles("test")
class OrderFacadeTest {

    @Autowired
    OrderFacade orderFacade;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    PointHistoryRepository pointHistoryRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    private void clean() {
        // 테스트 격리를 위해 테스트 테이블의 데이터를 전체 삭제한다.
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM point_histories");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM menus");
    }

    private Long createUser(long point) {
        User user = userRepository.save(User.of(UUID.randomUUID() + "@test.com", "테스터"));
        if (point > 0) {
            jdbcTemplate.update("UPDATE users SET point = ? WHERE id = ?", point, user.getId());
        }
        return user.getId();
    }

    private Long createMenu(String name, long price, int stock) {
        return menuRepository.save(Menu.of(name, price, stock)).getId();
    }

    private OrderRequest orderOf(Long userId, Long menuId, int quantity) {
        return new OrderRequest(userId, List.of(new OrderItemRequest(menuId, quantity)));
    }

    @Test
    void 잔액이_모자라면_동시_주문_중_하나만_성공한다() throws Exception {
        // given
        Long userId = createUser(10_000L);
        Long menuA = createMenu("A", 7_000L, 10);
        Long menuB = createMenu("B", 5_000L, 10);

        // when
        List<Throwable> failures = runConcurrently(2, i -> {
            Long menuId = (i == 0) ? menuA : menuB;
            orderFacade.order(orderOf(userId, menuId, 1), UUID.randomUUID().toString());
        });

        // then
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(BusinessException.class);
        assertThat(orderRepository.count()).isEqualTo(1L);

        long point = userRepository.findById(userId).orElseThrow().getPoint();
        assertThat(point).isGreaterThanOrEqualTo(0L);
        assertThat(point).isIn(3_000L, 5_000L);        // 7,000 또는 5,000 중 하나만 빠진다
    }

    @Test
    void 잔액이_넉넉하면_동시_주문이_모두_성공한다() throws Exception {
        // given
        Long userId = createUser(10_000L);
        Long menuA = createMenu("A", 3_000L, 10);
        Long menuB = createMenu("B", 4_000L, 10);

        // when
        List<Throwable> failures = runConcurrently(2, i -> {
            Long menuId = (i == 0) ? menuA : menuB;
            orderFacade.order(orderOf(userId, menuId, 1), UUID.randomUUID().toString());
        });

        // then
        assertThat(failures).isEmpty();
        assertThat(orderRepository.count()).isEqualTo(2L);
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(3_000L);
    }

    @Test
    void 재고보다_많은_동시_주문은_재고만큼만_성공한다() throws Exception {
        // given
        int stock = 10;
        int userCount = 50;
        Long menuId = createMenu("아메리카노", 1_000L, stock);
        List<Long> userIds = IntStream.range(0, userCount)
                .mapToObj(i -> createUser(10_000L))
                .toList();

        // when
        List<Throwable> failures = runConcurrently(userCount, i ->
                orderFacade.order(orderOf(userIds.get(i), menuId, 1), UUID.randomUUID().toString()));

        // then
        assertThat(failures).hasSize(userCount - stock);
        assertThat(orderRepository.count()).isEqualTo(stock);
        assertThat(menuRepository.findById(menuId).orElseThrow().getStock()).isZero();
    }

    @Test
    void 잔액도_재고도_부족하면_아무것도_바뀌지_않는다() {
        // given
        Long userId = createUser(1_000L);
        Long menuId = createMenu("비싼메뉴", 9_999L, 0);

        // when
        Throwable thrown = catchThrowable(() ->
                orderFacade.order(orderOf(userId, menuId, 1), UUID.randomUUID().toString()));

        // then
        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(orderRepository.count()).isZero();
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(1_000L);
        assertThat(menuRepository.findById(menuId).orElseThrow().getStock()).isZero();
        assertThat(pointHistoryRepository.count()).isZero();
    }

    @Test
    void 메뉴_셋_중_하나만_재고가_부족해도_전체가_롤백된다() {
        // given
        Long userId = createUser(100_000L);
        Long menuA = createMenu("A", 1_000L, 10);
        Long menuB = createMenu("B", 1_000L, 0);      // 품절
        Long menuC = createMenu("C", 1_000L, 10);
        OrderRequest request = new OrderRequest(userId, List.of(
                new OrderItemRequest(menuA, 1),
                new OrderItemRequest(menuB, 1),
                new OrderItemRequest(menuC, 1)));

        // when
        Throwable thrown = catchThrowable(() ->
                orderFacade.order(request, UUID.randomUUID().toString()));

        // then
        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(orderRepository.count()).isZero();
        assertThat(menuRepository.findById(menuA).orElseThrow().getStock()).isEqualTo(10);
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(100_000L);
    }

    @Test
    void 서로_반대_순서로_같은_메뉴들을_주문해도_데드락이_나지_않는다() throws Exception {
        // given
        Long userA = createUser(100_000L);
        Long userB = createUser(100_000L);
        Long menu1 = createMenu("A", 1_000L, 100);
        Long menu2 = createMenu("B", 1_000L, 100);

        OrderRequest forward = new OrderRequest(userA, List.of(
                new OrderItemRequest(menu1, 1), new OrderItemRequest(menu2, 1)));
        OrderRequest backward = new OrderRequest(userB, List.of(
                new OrderItemRequest(menu2, 1), new OrderItemRequest(menu1, 1)));

        // when
        List<Throwable> failures = runConcurrently(2, i ->
                orderFacade.order(i == 0 ? forward : backward, UUID.randomUUID().toString()));

        // then
        assertThat(failures).isEmpty();
        assertThat(orderRepository.count()).isEqualTo(2L);
        assertThat(menuRepository.findById(menu1).orElseThrow().getStock()).isEqualTo(98);
        assertThat(menuRepository.findById(menu2).orElseThrow().getStock()).isEqualTo(98);
    }

    @Test
    void 같은_키로_두_번_주문해도_한_번만_결제된다() {
        // given: 항목 2개짜리로 쓴다. fetch join 중복 행이 드러나는 자리다
        Long userId = createUser(100_000L);
        Long menuA = createMenu("A", 1_000L, 10);
        Long menuB = createMenu("B", 2_000L, 10);
        OrderRequest request = new OrderRequest(userId, List.of(
                new OrderItemRequest(menuA, 1), new OrderItemRequest(menuB, 2)));
        String key = UUID.randomUUID().toString();
        OrderResult first = orderFacade.order(request, key);

        // when
        OrderResult second = orderFacade.order(request, key);

        // then
        assertThat(first.created()).isTrue();
        assertThat(second.created()).isFalse();
        assertThat(second.response()).isEqualTo(first.response());    // balance 까지 같다
        assertThat(orderRepository.count()).isEqualTo(1L);
        assertThat(userRepository.findById(userId).orElseThrow().getPoint()).isEqualTo(95_000L);
    }

    @Test
    void 같은_키로_동시에_두_건을_보내도_주문은_하나다() throws Exception {
        // given
        Long userId = createUser(100_000L);
        Long menuId = createMenu("A", 1_000L, 10);
        String key = UUID.randomUUID().toString();
        List<OrderResult> results = Collections.synchronizedList(new ArrayList<>());

        // when
        List<Throwable> failures = runConcurrently(2, i ->
                results.add(orderFacade.order(orderOf(userId, menuId, 1), key)));

        // then
        assertThat(failures).isEmpty();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).response()).isEqualTo(results.get(1).response());
        assertThat(orderRepository.count()).isEqualTo(1L);
        assertThat(menuRepository.findById(menuId).orElseThrow().getStock()).isEqualTo(9);
    }

    @Test
    void 같은_키에_다른_항목_구성이면_거절한다() {
        // given
        Long userId = createUser(100_000L);
        Long menuId = createMenu("A", 1_000L, 10);
        String key = UUID.randomUUID().toString();
        orderFacade.order(orderOf(userId, menuId, 1), key);

        // when
        Throwable thrown = catchThrowable(() ->
                orderFacade.order(orderOf(userId, menuId, 3), key));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);

        assertThat(orderRepository.count()).isEqualTo(1L);
        assertThat(menuRepository.findById(menuId).orElseThrow().getStock()).isEqualTo(9);
    }
}