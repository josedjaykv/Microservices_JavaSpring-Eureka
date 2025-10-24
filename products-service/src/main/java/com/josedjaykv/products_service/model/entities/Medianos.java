package com.josedjaykv.products_service.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medianos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Medianos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private String name;
    private Float height;
    private Float weight;
    private String description;
    private Double price;
    private Boolean status;

    @Override
    public String toString() {
        return "Medianos{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
