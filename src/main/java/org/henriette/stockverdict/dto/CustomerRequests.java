package org.henriette.stockverdict.dto;

public class CustomerRequests {

    public record AddCustomerRequest(Long userId, String name, String phone, String email, String address) {}

    public record UpdateCustomerRequest(String name, String phone, String email, String address) {}

}
