package com.josedjaykv.products_service.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedianoResponse {

    private long id;
    private String sku;
    private String name;
    private Float height;
    private Float weight;
    private String description;
    private Double price;
    private Boolean status;
}
