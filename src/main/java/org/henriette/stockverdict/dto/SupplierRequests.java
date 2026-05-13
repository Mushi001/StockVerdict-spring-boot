package org.henriette.stockverdict.dto;

public class SupplierRequests {

    public record AddSupplierRequest(Long userId, String name, String phone, String email, String address, 
                                     String contactPerson, String notes, Double balanceOwed) {}

    public record UpdateSupplierRequest(Long userId, String name, String phone, String email, String address, 
                                        String contactPerson, String notes, Double balanceOwed) {}

}
