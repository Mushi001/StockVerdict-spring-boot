package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a customer in the StockVerdict system.
 * This entity stores details about a customer, including their contact information and the user who manages them.
 */
@Entity
@Table(name = "customers")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the customer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the customer.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The phone number of the customer.
     */
    private String phone;

    /**
     * The email address of the customer.
     */
    private String email;

    /**
     * The physical address of the customer.
     */
    private String address;

    /**
     * The timestamp when the customer record was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * The timestamp when the customer record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * The user (trader) who manages this customer.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // Which trader manages this customer

    // Constructors
    /**
     * Default constructor.
     * Initializes the creation and update timestamps to the current date and time.
     */
    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructs a new Customer with the specified details.
     *
     * @param name    the name of the customer
     * @param phone   the phone number of the customer
     * @param email   the email address of the customer
     * @param address the physical address of the customer
     * @param user    the user (trader) managing this customer
     */
    public Customer(String name, String phone, String email, String address, Users user) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
}