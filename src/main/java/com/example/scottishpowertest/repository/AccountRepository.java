package com.example.scottishpowertest.repository;

import com.example.scottishpowertest.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Integer> {
}

