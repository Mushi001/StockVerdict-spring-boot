package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * REST Controller for Dashboard analytics.
 * Provides endpoints for retrieving aggregate statistics and sales data for a specific trader.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Trader Dashboard", description = "Endpoints for retrieving trader-specific analytics, revenue, and inventory statistics.")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves the overall dashboard statistics for a specific user.
     * 
     * @param userId The ID of the user whose dashboard is being viewed.
     * @return ApiResponse containing a map of various statistics (total sales, revenue, low stock count, etc.).
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves aggregate data for the trader's dashboard including total sales, total revenue, and low stock warnings.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(@RequestParam Long userId) {
        Map<String, Object> stats = dashboardService.getDashboardStats(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, stats));
    }

    /**
     * Retrieves the recent sales history for a specific user.
     * 
     * @param userId The ID of the user.
     * @param limit The maximum number of recent sales to retrieve.
     * @return ApiResponse containing a map with the recent sales data.
     */
    @GetMapping("/recent-sales")
    @Operation(summary = "Get recent sales", description = "Retrieves a limited list of the most recent sales transactions completed by the trader.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentSales(@RequestParam Long userId,
                                                                             @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> data = dashboardService.getRecentSales(userId, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, data));
    }

    /**
     * Retrieves revenue statistics grouped by month for chart plotting.
     * 
     * @param userId The ID of the user.
     * @param year The year to retrieve monthly revenue for.
     * @return ApiResponse containing a map of months to revenue values.
     */
    @GetMapping("/revenue-by-month")
    @Operation(summary = "Get monthly revenue", description = "Retrieves revenue data grouped by month for plotting charts and graphs on the dashboard.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueByMonth(@RequestParam Long userId,
                                                                                @RequestParam int year) {
        Map<String, Object> data = dashboardService.getRevenueByMonth(userId, year);
        return ResponseEntity.ok(new ApiResponse<>(true, data));
    }
}
