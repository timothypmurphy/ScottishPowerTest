package com.example.scottishpowertest;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.entities.Account;
import com.example.scottishpowertest.entities.Reading;
import com.example.scottishpowertest.repository.AccountRepository;
import com.example.scottishpowertest.repository.ReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class LoadDatabase {


    private List<Reading> readings = new ArrayList<Reading>();

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(AccountRepository accountRepository, ReadingRepository readingRepository){
        Reading readingOne = new Reading(1, 123, new BigInteger("5456"), ReadingType.ELECTRIC, LocalDate.of(2022,1,1));
        readingOne.setAvgDailyUsage(new BigDecimal("25"));

        Reading readingTwo = new Reading(1, 456, BigInteger.valueOf(6456), ReadingType.GAS, LocalDate.of(2022,5,22));
        readingTwo.setAvgDailyUsage(new BigDecimal("4"));
//       Reading readingThree = new Reading(1, 789, 7456, ReadingType.ELECTRIC, LocalDate.of(2022,12,31)),
        Reading readingFour = new Reading(2, 654, BigInteger.valueOf(8456), ReadingType.GAS, LocalDate.of(2023,2,4));
        readingFour.setAvgDailyUsage(new BigDecimal("97"));

        readings.add(readingOne);
        readings.add(readingTwo);
        readings.add(readingFour);

        return args -> {
            log.info("" + accountRepository.save(new Account()));
            log.info("" + accountRepository.save(new Account()));
            log.info("" + readingRepository.saveAll(readings));
        };
    }
}
