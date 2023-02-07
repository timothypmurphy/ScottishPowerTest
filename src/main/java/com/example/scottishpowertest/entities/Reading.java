package com.example.scottishpowertest.entities;

import com.example.scottishpowertest.domain.ReadingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity(name="READINGS")
@Data
public class Reading {

    public Reading (int accountId, int meterId, BigInteger reading, ReadingType readingType, LocalDate date){
        this.accountId = accountId;
        this.meterId = meterId;
        this.reading = reading;
        this.readingType = readingType;
        this.date = date;
    }

    public Reading() {

    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Id Integer id;

    private int accountId;
    private int meterId;
    private BigInteger reading;

    private ReadingType readingType;

    @JsonFormat(pattern="dd-MM-yyyy")
    private LocalDate date;

    private BigInteger usageSinceLastRead;
    private BigDecimal periodSinceLastRead;
    private BigDecimal avgDailyUsage;
}

