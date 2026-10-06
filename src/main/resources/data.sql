-- 사용자
INSERT INTO users (id, email, name, point, created_at)
VALUES (1, 'seed@test.com', 'seed-user', 903500, NOW() - INTERVAL 30 DAY),
       (2, 'tester1@test.com', 'tester1', 0, NOW() - INTERVAL 30 DAY),
       (3, 'tester2@test.com', 'tester2', 0, NOW() - INTERVAL 30 DAY);

-- 메뉴 (말차라떼만 재고 3, 나머지는 주문된 만큼 차감된 상태)
INSERT INTO menus (id, name, price, stock, created_at)
VALUES (1, '아메리카노', 4500, 95, NOW() - INTERVAL 30 DAY),
       (2, '카페라떼', 5000, 97, NOW() - INTERVAL 30 DAY),
       (3, '콜드브루', 5500, 97, NOW() - INTERVAL 30 DAY),
       (4, '카푸치노', 5000, 98, NOW() - INTERVAL 30 DAY),
       (5, '바닐라라떼', 5500, 99, NOW() - INTERVAL 30 DAY),
       (6, '에스프레소', 3500, 94, NOW() - INTERVAL 30 DAY),
       (7, '카라멜마키아토', 6000, 100, NOW() - INTERVAL 30 DAY),
       (8, '아인슈페너', 6500, 100, NOW() - INTERVAL 30 DAY),
       (9, '자몽에이드', 6000, 0, NOW() - INTERVAL 30 DAY),
       (10, '말차라떼', 6000, 2, NOW() - INTERVAL 30 DAY);

-- 주문: 7일 바깥 (집계에서 제외되어야 함)
INSERT INTO orders (id, user_id, idempotency_key, total_amount, balance_after, created_at)
VALUES (1, 1, 'seed-1', 3500, 996500, NOW() - INTERVAL 10 DAY),
       (2, 1, 'seed-2', 3500, 993000, NOW() - INTERVAL 10 DAY),
       (3, 1, 'seed-3', 3500, 989500, NOW() - INTERVAL 9 DAY),
       (4, 1, 'seed-4', 3500, 986000, NOW() - INTERVAL 9 DAY),
       (5, 1, 'seed-5', 3500, 982500, NOW() - INTERVAL 8 DAY),
       (6, 1, 'seed-6', 3500, 979000, NOW() - INTERVAL 180 HOUR);

-- 주문: 7일 안쪽 (집계 대상)
INSERT INTO orders (id, user_id, idempotency_key, total_amount, balance_after, created_at)
VALUES (7, 1, 'seed-7', 4500, 974500, NOW() - INTERVAL 156 HOUR),
       (8, 1, 'seed-8', 9500, 965000, NOW() - INTERVAL 5 DAY),
       (9, 1, 'seed-9', 4500, 960500, NOW() - INTERVAL 4 DAY),
       (10, 1, 'seed-10', 10000, 950500, NOW() - INTERVAL 3 DAY),
       (11, 1, 'seed-11', 4500, 946000, NOW() - INTERVAL 2 DAY),
       (12, 1, 'seed-12', 10500, 935500, NOW() - INTERVAL 2 DAY),
       (13, 1, 'seed-13', 10500, 925000, NOW() - INTERVAL 1 DAY),
       (14, 1, 'seed-14', 10000, 915000, NOW() - INTERVAL 1 DAY),
       (15, 1, 'seed-15', 11500, 903500, NOW() - INTERVAL 6 HOUR);
-- 주문 항목
INSERT INTO order_items (order_id, menu_id, quantity, line_amount)
VALUES (1, 6, 1, 3500),
       (2, 6, 1, 3500),
       (3, 6, 1, 3500),
       (4, 6, 1, 3500),
       (5, 6, 1, 3500),
       (6, 6, 1, 3500),
       (7, 1, 1, 4500),
       (8, 1, 1, 4500),
       (8, 2, 1, 5000),
       (9, 1, 1, 4500),
       (10, 1, 1, 4500),
       (10, 3, 1, 5500),
       (11, 1, 1, 4500),
       (12, 2, 1, 5000),
       (12, 3, 1, 5500),
       (13, 3, 1, 5500),
       (13, 4, 1, 5000),
       (14, 2, 1, 5000),
       (14, 4, 1, 5000),
       (15, 5, 1, 5500),
       (15, 10, 1, 6000);

-- 포인트 이력 (amount 합이 users.point와, balance_after가 누적과 일치해야 한다)
INSERT INTO point_histories (user_id, type, amount, balance_after, created_at)
VALUES (1, 'CHARGE', 1000000, 1000000, NOW() - INTERVAL 30 DAY),
       (1, 'USE', -3500, 996500, NOW() - INTERVAL 10 DAY),
       (1, 'USE', -3500, 993000, NOW() - INTERVAL 10 DAY),
       (1, 'USE', -3500, 989500, NOW() - INTERVAL 9 DAY),
       (1, 'USE', -3500, 986000, NOW() - INTERVAL 9 DAY),
       (1, 'USE', -3500, 982500, NOW() - INTERVAL 8 DAY),
       (1, 'USE', -3500, 979000, NOW() - INTERVAL 180 HOUR),
       (1, 'USE', -4500, 974500, NOW() - INTERVAL 156 HOUR),
       (1, 'USE', -9500, 965000, NOW() - INTERVAL 5 DAY),
       (1, 'USE', -4500, 960500, NOW() - INTERVAL 4 DAY),
       (1, 'USE', -10000, 950500, NOW() - INTERVAL 3 DAY),
       (1, 'USE', -4500, 946000, NOW() - INTERVAL 2 DAY),
       (1, 'USE', -10500, 935500, NOW() - INTERVAL 2 DAY),
       (1, 'USE', -10500, 925000, NOW() - INTERVAL 1 DAY),
       (1, 'USE', -10000, 915000, NOW() - INTERVAL 1 DAY),
       (1, 'USE', -11500, 903500, NOW() - INTERVAL 6 HOUR);