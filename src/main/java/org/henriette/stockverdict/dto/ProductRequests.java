package org.henriette.stockverdict.dto;

public class ProductRequests {

    public record AddProductRequest(Long userId, String name, String description, String barcode, 
                                    Double purchasePrice, Double sellingPrice, Integer quantityInStock, 
                                    Integer reorderLevel, Long supplierId) {}

    public record UpdateProductRequest(String name, String description, String barcode, 
                                       Double purchasePrice, Double sellingPrice, Integer quantityInStock, 
                                       Integer reorderLevel, Long supplierId) {}

}
