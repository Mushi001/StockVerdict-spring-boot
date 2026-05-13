package org.henriette.stockverdict.dto;

public class StockEntryRequests {

    public record AddStockEntryRequest(Long userId, Long productId, Long supplierId, 
                                       Integer quantityAdded, Double purchasePrice) {}

}
