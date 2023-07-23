package com.decerto.leszek.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String surname;

    @OneToMany
    @JoinColumn(name= "customer_id")
    private List<Address> addresses;

}
