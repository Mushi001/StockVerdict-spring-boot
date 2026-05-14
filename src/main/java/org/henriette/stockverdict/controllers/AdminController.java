package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.AdminService;
import org.henriette.stockverdict.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.AdminRequests.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Administrator operations.
 * Provides endpoints for viewing platform analytics and managing users/traders.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Operations", description = "Endpoints for platform administrators to manage users and view analytics.")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    /**
     * Retrieves high-level analytics for the admin dashboard.
     * 
     * @return ApiResponse containing map of dashboard statistics.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard analytics", description = "Retrieves high-level platform statistics including active users, total revenue, and system health.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardAnalytics() {
        Map<String, Object> analytics = adminService.getDashboardAnalytics();
        return ResponseEntity.ok(new ApiResponse<>(true, analytics));
    }

    /**
     * Updates the authorization status of a trader (e.g., ACTIVE, PENDING, INACTIVE).
     * 
     * @param id The unique identifier of the user/trader.
     * @param request The data transfer object containing the new status.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/traders/{id}/status")
    @Operation(summary = "Update user status", description = "Approves, rejects, or suspends a trader's account by updating their status.")
    public ResponseEntity<ApiResponse<Void>> updateTraderStatus(@PathVariable Long id,
                                                                  @RequestBody UpdateTraderStatusRequest request) {
        String status = request.status();
        if (status == null || (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("PENDING"))) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid status"));
        }

        if (userService.updateUserStatus(id, status)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Trader status updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Trader not found or update failed"));
    }

    /**
     * Retrieves a list of all users filtered by a specific role.
     * 
     * @param role The role to filter by (e.g., TRADER, ADMIN).
     * @return ApiResponse containing the list of filtered users.
     */
    @GetMapping("/users")
    @Operation(summary = "Get users by role", description = "Retrieves a list of all users filtered by their assigned platform role.")
    public ResponseEntity<ApiResponse<List<Users>>> getUsersByRole(@RequestParam String role) {
        List<Users> users = adminService.getUsersByRole(role);
        return ResponseEntity.ok(new ApiResponse<>(true, users));
    }
}
