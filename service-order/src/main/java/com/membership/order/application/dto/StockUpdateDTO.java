package com.membership.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour mettre à jour le stock d'un produit via le service Product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateDTO {

    private Integer quantity;
    private String operation; // "ADD", "SUBTRACT", "SET"
}
