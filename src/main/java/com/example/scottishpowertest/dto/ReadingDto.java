package com.example.scottishpowertest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class ReadingDto {
    private Integer id;
    @NotNull(message = "Meter id is required")
    private Integer meterId;
    @NotNull(message = "You must provide a reading")
    private BigInteger reading;
    @PastOrPresent
    @JsonFormat(pattern="dd-MM-yyyy")
    private LocalDate date;
    private BigInteger usageSinceLastRead;
    private BigDecimal periodSinceLastRead;
    private BigDecimal avgDailyUsage;
}
