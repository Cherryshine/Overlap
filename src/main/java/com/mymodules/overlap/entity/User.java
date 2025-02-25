package com.mymodules.overlap.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@DiscriminatorColumn(name = "user_type")
public abstract class User {
    @Id
    private Long id;
    private String username;
    private String password;
}
