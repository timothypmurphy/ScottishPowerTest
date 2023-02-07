package com.example.scottishpowertest.exception;

import com.example.scottishpowertest.domain.ReadingType;

import java.time.LocalDate;

public class HistoricReadingFoundException extends RuntimeException{
    public HistoricReadingFoundException(ReadingType readingType, LocalDate date){
        super(String.format("Reading of type %s and date %s is dated before the previous reading on this account, see logs for more info", readingType, date.toString()));
    }
}
