package org.henriette.stockverdict.dto;

import java.util.List;

public class SaleRequests {

    public record SaleItemRequest(Long productId, Integer quantity) {}

    public record CreateSaleRequest(Long userId, String paymentMethod, String customerId, 
                                    String newCustomerName, String newCustomerPhone, 
                                    List<SaleItemRequest> items) {}

    public record UpdateSaleRequest(Long productId, Integer quantity, String paymentMethod, Long customerId) {}

}
