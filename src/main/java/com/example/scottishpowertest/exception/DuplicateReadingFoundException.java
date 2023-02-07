package com.example.scottishpowertest.exception;

import com.example.scottishpowertest.domain.ReadingType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DuplicateReadingFoundException extends RuntimeException{
    public DuplicateReadingFoundException(ReadingType readingType, LocalDate date){
        super(String.format("Duplicate reading of type %s and date %s found for this account, see logs for more info", readingType, date.toString()));
    }
}
