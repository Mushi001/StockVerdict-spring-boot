package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a product available for sale.
 */
@Entity
@Table(name = "products")
public class Products implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the product.
     */
    @Column(nullable = false)
    private String name;

    /**
     * A detailed description of the product.
     */
    private String description;

    /**
     * The barcode associated with the product for scanning.
     */
    private String barcode;

    /**
     * The price at which the product is purchased from the supplier.
     */
    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;

    /**
     * The price at which the product is sold to customers.
     */
    @Column(name = "selling_price", nullable = false)
    private Double sellingPrice;

    /**
     * The current quantity available in stock.
     */
    @Column(name = "quantity_in_stock")
    private Integer quantityInStock;

    /**
     * The minimum quantity threshold before a restock is needed.
     */
    @Column(name = "reorder_level")
    private Integer reorderLevel;

    /**
     * The timestamp when the product was added to the system.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * The timestamp when the product record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * The user who manages or created this product record.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    /**
     * The supplier providing this product.
     */
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /**
     * The list of sale items associated with this product.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> saleItems;

    /**
     * The history of stock entries for this product.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockEntry> stockEntries;

    // Constructors
    /**
     * Default constructor.
     * Initializes the creation and update timestamps to the current date and time.
     */
    public Products() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructs a new Product with the specified details.
     *
     * @param name            the name of the product
     * @param description     the description of the product
     * @param barcode         the product's barcode
     * @param purchasePrice   the purchase price from the supplier
     * @param sellingPrice    the selling price to customers
     * @param quantityInStock the initial stock quantity
     * @param reorderLevel    the threshold for reordering
     * @param user            the associated user managing the product
     * @param supplier        the supplier of the product
     */
    public Products(String name, String description, String barcode, Double purchasePrice, Double sellingPrice, Integer quantityInStock, Integer reorderLevel, Users user, Supplier supplier) {
        this.name = name;
        this.description = description;
        this.barcode = barcode;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.user = user;
        this.supplier = supplier;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public List<SaleItem> getSaleItems() { return saleItems; }
    public void setSaleItems(List<SaleItem> saleItems) { this.saleItems = saleItems; }

    public List<StockEntry> getStockEntries() { return stockEntries; }
    public void setStockEntries(List<StockEntry> stockEntries) { this.stockEntries = stockEntries; }
}