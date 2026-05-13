package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Customer;
import org.henriette.stockverdict.repositories.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Customer entities.
 * Provides CRUD operations and custom queries for customer management.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Adds a new customer to the database.
     */
    @Transactional
    public boolean addCustomer(Customer customer) {
        try {
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing customer's details.
     */
    @Transactional
    public boolean updateCustomer(Customer updatedCustomer) {
        try {
            Optional<Customer> optionalCustomer = customerRepository.findById(updatedCustomer.getId());
            if (optionalCustomer.isEmpty()) return false;

            Customer existing = optionalCustomer.get();
            existing.setName(updatedCustomer.getName());
            existing.setPhone(updatedCustomer.getPhone());
            existing.setEmail(updatedCustomer.getEmail());
            existing.setAddress(updatedCustomer.getAddress());
            existing.setUpdatedAt(LocalDateTime.now());

            customerRepository.save(existing);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a customer by their ID.
     */
    @Transactional
    public boolean deleteCustomer(Long customerId) {
        try {
            if (!customerRepository.existsById(customerId)) return false;
            customerRepository.deleteById(customerId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a customer by their ID.
     */
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    /**
     * Retrieves all customers managed by a specific user.
     */
    public List<Customer> getCustomersByUser(Long userId) {
        return customerRepository.findByUserIdOrderByNameAsc(userId);
    }

    /**
     * Retrieves all customers in the system.
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllByOrderByNameAsc();
    }

    /**
     * Searches for customers managed by a specific user using a keyword.
     */
    public List<Customer> searchCustomers(Long userId, String keyword) {
        return customerRepository.searchByUserAndKeyword(userId, keyword);
    }

    /**
     * Checks if an email is already in use by another customer.
     */
    public boolean isEmailExists(String email, Long excludeCustomerId) {
        if (excludeCustomerId != null) {
            return customerRepository.existsByEmailAndIdNot(email, excludeCustomerId);
        }
        return customerRepository.existsByEmail(email);
    }

    /**
     * Counts the total number of customers managed by a specific user.
     */
    public Long countCustomersByUser(Long userId) {
        return customerRepository.countByUserId(userId);
    }
}
