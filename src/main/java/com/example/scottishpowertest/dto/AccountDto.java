package com.example.scottishpowertest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountDto {
    private Integer accountId;
    @Valid
    private List<ReadingDto> gasReadings;
    @Valid
    private List<ReadingDto> elecReadings;

    private BigDecimal gasComparison;
    private BigDecimal elecComparison;
}
