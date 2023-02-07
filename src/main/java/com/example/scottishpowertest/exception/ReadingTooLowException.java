package com.example.scottishpowertest.exception;

import com.example.scottishpowertest.domain.ReadingType;

import java.time.LocalDate;

public class ReadingTooLowException extends RuntimeException{
    public ReadingTooLowException(ReadingType readingType, LocalDate date){
        super(String.format("Reading of type %s and date %s is lower than the previous reading on this account, see logs for more info", readingType, date.toString()));
    }
}
