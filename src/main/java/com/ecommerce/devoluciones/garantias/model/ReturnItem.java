package com.ecommerce.devoluciones.garantias.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "return_items")
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_r_item")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_request", nullable = false)
    @JsonBackReference
    private ReturnRequest returnRequest;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private ReturnReason reason;


    public ReturnItem(ReturnRequest returnRequest, Integer itemId, String itemName, Integer quantity, ReturnReason reason) {
        this.returnRequest = returnRequest;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.reason = reason;
    }
}
