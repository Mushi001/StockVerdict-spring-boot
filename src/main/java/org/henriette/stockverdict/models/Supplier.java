package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a supplier or vendor who provides products to the business.
 * Contains supplier contact details, balance owed, and associations to products and stock entries.
 */
@Entity
@Table(name = "suppliers")
public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the supplier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the supplier or the supplying company.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The primary phone number of the supplier.
     */
    private String phone;

    /**
     * The primary email address of the supplier.
     */
    private String email;

    /**
     * The physical address of the supplier.
     */
    private String address;

    /**
     * The name of the primary contact person at the supplier's company.
     */
    @Column(name = "contact_person")
    private String contactPerson;

    /**
     * The outstanding balance owed to this supplier.
     */
    @Column(name = "balance_owed")
    private double balanceOwed;

    /**
     * Additional notes or descriptions about the supplier.
     */
    private String notes;

    /**
     * The user who manages the relationship with this supplier.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    /**
     * A list of all stock entries fulfilled by this supplier.
     */
    @OneToMany(mappedBy = "supplier")
    private List<StockEntry> stockEntries;

    /**
     * A list of products typically supplied by this vendor.
     */
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Products> products;

    // Constructors
    /**
     * Default constructor.
     */
    public Supplier() {}

    /**
     * Constructs a new Supplier with the specified details.
     *
     * @param name          the name of the supplier
     * @param phone         the contact phone number
     * @param email         the contact email address
     * @param address       the physical address
     * @param contactPerson the name of the main point of contact
     * @param balanceOwed   the starting balance owed
     * @param notes         additional notes about the relationship
     * @param user          the user creating or managing this supplier record
     */
    public Supplier(String name, String phone, String email, String address, String contactPerson, double balanceOwed, String notes, Users user) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.contactPerson = contactPerson;
        this.balanceOwed = balanceOwed;
        this.notes = notes;
        this.user = user;
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

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public double getBalanceOwed() { return balanceOwed; }
    public void setBalanceOwed(double balanceOwed) { this.balanceOwed = balanceOwed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public List<StockEntry> getStockEntries() { return stockEntries; }
    public void setStockEntries(List<StockEntry> stockEntries) { this.stockEntries = stockEntries; }

    public List<Products> getProducts() { return products; }
    public void setProducts(List<Products> products) { this.products = products; }
}