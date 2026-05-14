package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Sales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for retrieving analytical data for the trader dashboard.
 */
@Service
public class DashboardService {

    private final SaleService saleService;
    private final ProductService productService;

    @Autowired
    public DashboardService(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
    }

    /**
     * Aggregates stats like total sales, revenue, and low stock items.
     */
    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalSales = saleService.countSalesByUser(userId);
        Double totalRevenue = saleService.getTotalRevenueByUser(userId);
        int lowStockCount = productService.getLowStockProducts(userId).size();
        
        stats.put("totalSales", totalSales);
        stats.put("totalRevenue", totalRevenue);
        stats.put("lowStockCount", lowStockCount);
        
        return stats;
    }

    /**
     * Retrieves the most recent sales for a user, up to a specified limit.
     */
    public Map<String, Object> getRecentSales(Long userId, int limit) {
        Map<String, Object> data = new HashMap<>();
        List<Sales> allSales = saleService.getSalesByUser(userId);
        List<Sales> recentSales = allSales.size() > limit ? allSales.subList(0, limit) : allSales;
        
        data.put("recentSales", recentSales);
        return data;
    }

    /**
     * Retrieves revenue aggregated by month for a given year.
     */
    public Map<String, Object> getRevenueByMonth(Long userId, int year) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Double> monthlyRevenue = new HashMap<>();
        
        for (int i = 1; i <= 12; i++) {
            LocalDateTime start = LocalDateTime.of(year, i, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            Double revenue = saleService.getTotalRevenueByDateRange(userId, start, end);
            monthlyRevenue.put(start.getMonth().name(), revenue != null ? revenue : 0.0);
        }
        
        data.put("monthlyRevenue", monthlyRevenue);
        return data;
    }
}
