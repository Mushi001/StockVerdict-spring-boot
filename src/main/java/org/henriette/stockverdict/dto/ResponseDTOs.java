package org.henriette.stockverdict.dto;

import org.henriette.stockverdict.models.SaleItem;
import org.henriette.stockverdict.models.Sales;

import java.util.List;
import java.util.Map;

public class ResponseDTOs {

    // For simple success/error messages
    public record GenericResponse(boolean success, String message) {}

    // For returning a list of entities (e.g., getSales, getProducts)
    public record ListResponse<T>(boolean success, int count, List<T> data) {}

    // For returning a single entity (e.g., getProductById, getCustomerById)
    public record SingleItemResponse<T>(boolean success, T data) {}

    // For Authentication Responses (Login, Verify OTP)
    public record AuthResponse(boolean success, String message, String token, Map<String, Object> user) {}
    
    // For Dashboard data
    public record DashboardResponse(boolean success, Map<String, Object> data) {}

    // For specific complex responses like Sale + Items
    public record SaleDetailResponse(boolean success, Sales sale, List<SaleItem> items) {}
    
    // For specific endpoints returning counts only
    public record CountResponse(boolean success, Long count) {}
    
    // For User Role counts
    public record UserRoleListResponse<T>(boolean success, int count, List<T> users) {}
    
    // For Payment Info
    public record PaymentInfoResponse(boolean success, Map<String, Object> paymentInfo) {}
    
    // For Admin Dashboard
    public record AdminDashboardResponse(boolean success, Map<String, Object> data) {}
}
