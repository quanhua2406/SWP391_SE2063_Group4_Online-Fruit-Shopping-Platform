# Kết quả hiện tại — 12/11/2026

Xem PROMOTION_CUSTOMER_PRICING.md: 173 tests, 163 đạt, 10 lỗi delivery cũ; 22 test promotion đạt.

## Báo cáo bản gộp ban đầu (10/11/2026)

# Kết quả kiểm tra promotion merge

- Java 17: compile source và test thành công.
- `mvn clean verify -DskipTests=false`: 165 tests, 10 failures, 0 errors (155 đạt).
- Bản ZIP gốc: 164 tests, đúng cùng 10 failures, 0 errors.
- PromotionRulesTest (9), PromotionDatabaseTest (3), CampaignPromotionServiceTest (2): đạt.
- AccessPolicyTest (43), AuthorizationFilterTest (18, có kiểm tra đường dẫn mới): đạt.
- JspCompilationTest: đạt, biên dịch 21 trang JSP gồm các form promotion, dashboard và pricing.
- `mvn package -DskipTests=true`: BUILD SUCCESS. Đây là kiểm tra đóng gói; không thay thế kết quả toàn bộ test ở trên.
- Không có mapping servlet trùng trong source.
- Chưa kiểm tra tương tác trực tiếp trên Tomcat + SQL Server thực tế; database test dùng H2.

## Những test lỗi sẵn có ở cả bản gốc và bản gộp

- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.acceptedAssignmentCannotThenBeRejected
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.codCompletionCommitsAllEffectsAndRejectsReplay
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.codWithoutCollectionLeavesEverythingUnchanged
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.inactiveOrUnassignedShipperCannotChangeDelivery
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.onlinePendingOrWrongAmountCannotComplete
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.pickupStartFailureAndRetrySynchronizeCanonicalStates
- com.mycompany.onlinefruitshopping.dao.delivery.DeliveryTransactionTest.rejectedAttemptClosesAndReassignmentKeepsOneBaseDelivery
- com.mycompany.onlinefruitshopping.dao.delivery.MergedDeliverySchemaTest.acceptsRejectsAndCompletionWriteCanonicalColumns
- com.mycompany.onlinefruitshopping.dao.delivery.ShipperTeamDatabaseTest.terminationWaitsForExistingDeliveryButBlocksNewWork
- com.mycompany.onlinefruitshopping.dao.delivery.ShipperTeamDatabaseTest.voluntaryLeaveAlsoWaitsForDeliveryCompletion
