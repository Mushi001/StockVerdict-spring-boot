package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an entry of stock (inventory) for a product.
 * Tracks the quantity added, the purchase price, the product, the supplier, 
 * and the user who generated the entry.
 */
@Entity
@Table(name = "stock_entries")
public class StockEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the stock entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The number of items added to the stock.
     */
    @Column(name = "quantity_added")
    private Integer quantityAdded;

    /**
     * The price at which the stock was purchased.
     */
    @Column(name = "purchase_price")
    private Double purchasePrice;

    /**
     * The date and time when the stock was added.
     */
    @Column(name = "date_added")
    private LocalDateTime dateAdded;

    /**
     * The product for which stock is being added.
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product;

    /**
     * The supplier from whom the stock was purchased.
     */
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /**
     * The user who recorded this stock entry.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    // Constructors
    /**
     * Default constructor.
     * Automatically sets the date added to the current date and time.
     */
    public StockEntry() {
        this.dateAdded = LocalDateTime.now();
    }

    /**
     * Constructs a new StockEntry with the specified details.
     *
     * @param quantityAdded the amount of stock initially added
     * @param purchasePrice the purchase price of the added stock
     * @param product       the product being restocked
     * @param supplier      the supplier providing the stock
     * @param user          the user handling the entry creation
     */
    public StockEntry(Integer quantityAdded, Double purchasePrice, Products product, Supplier supplier, Users user) {
        this.quantityAdded = quantityAdded;
        this.purchasePrice = purchasePrice;
        this.product = product;
        this.supplier = supplier;
        this.user = user;
        this.dateAdded = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(Integer quantityAdded) { this.quantityAdded = quantityAdded; }

    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }

    public Products getProduct() { return product; }
    public void setProduct(Products product) { this.product = product; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
}
