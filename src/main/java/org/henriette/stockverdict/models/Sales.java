package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a completed sales transaction in the system.
 * It contains information about the sale date, total amount, the trader who made the sale, 
 * the customer (optionally), and the individual items sold.
 */
@Entity
@Table(name = "sales")
public class Sales implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the sales transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The date and time when the sale occurred.
     */
    @Column(name = "sale_date")
    private LocalDateTime saleDate;

    /**
     * The total monetary value of the sale.
     */
    @Column(name = "total_amount")
    private Double totalAmount;

    /**
     * The method of payment used by the customer.
     */
    @Column(name = "payment_method")
    private String paymentMethod;

    /**
     * The trader who handled the sale transaction.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;  // The trader who handled the sale

    /**
     * The optional customer who made the purchase.
     */
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;  // Optional: the customer who made the purchase

    /**
     * The list of items included in this sales transaction.
     */
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<SaleItem> saleItems;  // Products in this sale

    // Constructors
    /**
     * Default constructor.
     * Automatically sets the sale date to the current date and time.
     */
    public Sales() {
        this.saleDate = LocalDateTime.now();
    }

    /**
     * Constructs a new Sales transaction with the specified details.
     *
     * @param totalAmount   the total amount of the sale
     * @param paymentMethod the payment method used
     * @param user          the user handling the sale
     * @param customer      the customer making the purchase (can be null)
     */
    public Sales(Double totalAmount, String paymentMethod, Users user, Customer customer) {
        this.saleDate = LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.customer = customer;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<SaleItem> getSaleItems() { return saleItems; }
    public void setSaleItems(List<SaleItem> saleItems) { this.saleItems = saleItems; }
}