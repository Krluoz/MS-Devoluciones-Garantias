package com.ecommerce.devoluciones.garantias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "warranty_items")
public class WarrantyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_w_item")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_claim", nullable = false)
    private WarrantyClaim claim;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private WarrantyReason reason;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_expires_at", nullable = false)
    private LocalDateTime warrantyExpiresAt;

    public WarrantyItem(Long itemId, String itemName, Integer quantity,
                        WarrantyReason reason, String description,
                        LocalDateTime purchaseDate, LocalDateTime warrantyExpiresAt) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.reason = reason;
        this.description = description;
        this.purchaseDate = purchaseDate;
        this.warrantyExpiresAt = warrantyExpiresAt;
    }
}