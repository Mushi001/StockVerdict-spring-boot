package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for administrative analytics and user management operations.
 */
@Service
public class AdminService {

    private final UserService userService;
    private final SaleService saleService;

    @Autowired
    public AdminService(UserService userService, SaleService saleService) {
        this.userService = userService;
        this.saleService = saleService;
    }

    /**
     * Retrieves aggregated system-wide analytics for the admin dashboard.
     */
    public Map<String, Object> getDashboardAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        Long activeUsers = userService.countUsersByStatus("ACTIVE");
        Long pendingUsers = userService.countUsersByStatus("PENDING");
        Double totalRevenue = saleService.getSystemWideTotalRevenue();
        
        analytics.put("activeUsers", activeUsers);
        analytics.put("pendingUsers", pendingUsers);
        analytics.put("totalRevenue", totalRevenue);
        analytics.put("systemHealth", "Good");
        
        return analytics;
    }

    /**
     * Retrieves all users filtered by their role.
     */
    public List<Users> getUsersByRole(String role) {
        return userService.getAllUsersByRole(role);
    }
}
