package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.SaleService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Admin functionality.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final SaleService saleService;

    @Autowired
    public AdminController(UserService userService, SaleService saleService) {
        this.userService = userService;
        this.saleService = saleService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        List<Users> allTraders = userService.getAllUsersByRole("TRADER");
        data.put("totalTraders", allTraders.size());
        data.put("pendingTraders", userService.countUsersByStatus("PENDING"));
        data.put("inactiveTraders", userService.countUsersByStatus("INACTIVE"));
        data.put("monthlySales", saleService.getSystemWideTotalRevenue());

        List<Object[]> rawTopProducts = saleService.getSystemWideTopSellingProducts(5);
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Object[] row : rawTopProducts) {
            topProducts.add(Map.of("name", row[0], "category", "General", "unitsSold", row[1]));
        }
        data.put("topProducts", topProducts);

        List<Object[]> rawTopTraders = saleService.getSystemWideTopTraders(5);
        List<Map<String, Object>> topTraders = new ArrayList<>();
        for (Object[] row : rawTopTraders) {
            topTraders.add(Map.of("fullName", row[0], "totalSales", row[1]));
        }
        data.put("topTraders", topTraders);

        List<Map<String, Object>> tradersList = new ArrayList<>();
        for (Users u : allTraders) {
            Map<String, Object> tm = new HashMap<>();
            tm.put("id", u.getId());
            tm.put("fullName", u.getName());
            tm.put("email", u.getEmail());
            tm.put("joinDate", u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "N/A");
            tm.put("status", u.getStatus() != null ? u.getStatus().toLowerCase() : "pending");
            tradersList.add(tm);
        }
        data.put("traders", tradersList);

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PutMapping("/traders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateTraderStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("PENDING"))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid status"));
        }

        if (userService.updateUserStatus(id, status)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Trader not found or update failed"));
    }

    @DeleteMapping("/traders/{id}")
    public ResponseEntity<Map<String, Object>> deleteTrader(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Trader deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Trader not found"));
    }
}
