package com.example.scottishpowertest.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity(name="ACCOUNTS")
@Data
public class Account {

    public Account() {

    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Id int accountId;

    @OneToMany(mappedBy = "accountId")
    private List<Reading> readings;

}
