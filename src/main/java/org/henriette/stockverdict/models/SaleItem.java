package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Represents an individual item within a sale transaction.
 * Associates a product with a specific sale, recording quantity and price at the time of sale.
 */
@Entity
@Table(name = "sale_items")
public class SaleItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the sale item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The quantity of the product purchased in this sale item.
     */
    private Integer quantity;

    /**
     * The price of the product at the time of the sale.
     */
    @Column(name = "price_at_sale")
    private Double priceAtSale;

    /**
     * The subtotal for this item (quantity * priceAtSale).
     */
    private Double subtotal;

    /**
     * The sale transaction to which this item belongs.
     */
    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sales sale;

    /**
     * The product being sold in this item.
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product;

    // Constructors
    /**
     * Default constructor.
     */
    public SaleItem() {}

    /**
     * Constructs a new SaleItem with the specified details, calculating the subtotal automatically.
     *
     * @param quantity    the quantity sold
     * @param priceAtSale the price per unit at the time of sale
     * @param sale        the parent sale transaction
     * @param product     the product being sold
     */
    public SaleItem(Integer quantity, Double priceAtSale, Sales sale, Products product) {
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
        this.subtotal = quantity * priceAtSale;
        this.sale = sale;
        this.product = product;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(Double priceAtSale) { this.priceAtSale = priceAtSale; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Sales getSale() { return sale; }
    public void setSale(Sales sale) { this.sale = sale; }

    public Products getProduct() { return product; }
    public void setProduct(Products product) { this.product = product; }
}