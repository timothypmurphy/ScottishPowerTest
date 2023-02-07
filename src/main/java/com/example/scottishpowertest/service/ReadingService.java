package com.example.scottishpowertest.service;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.dto.ReadingDto;
import com.example.scottishpowertest.exception.DuplicateReadingFoundException;
import com.example.scottishpowertest.exception.HistoricReadingFoundException;
import com.example.scottishpowertest.exception.ReadingTooLowException;
import com.example.scottishpowertest.entities.Reading;
import com.example.scottishpowertest.repository.ReadingRepository;
import org.apache.logging.slf4j.SLF4JLogger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReadingService {

    private final static Logger log = LoggerFactory.getLogger(SLF4JLogger.class);

    private final ReadingRepository readingRepository;

    @Autowired
    private ModelMapper modelMapper;

    ReadingService(final ReadingRepository readingRepository, final ModelMapper modelMapper) {
        this.readingRepository = readingRepository;
        this.modelMapper = modelMapper;
    };

    public void saveReading(final ReadingDto readingGetDto, final ReadingType readingType, final int accountId){
        final Reading convertedReading = convertToEntity(readingGetDto, readingType, accountId);
        validateReading(convertedReading, accountId);
        readingRepository.save(convertedReading);
    }

    public void validateReading(Reading reading, int accountId){
        final List<Reading> accountReadings = readingRepository.findAllByAccountIdAndReadingTypeOrderByDateAsc(accountId, reading.getReadingType());
        final Boolean duplicateFound = accountReadings.stream()
                .anyMatch(r -> r.getDate().equals(reading.getDate()));
        if (duplicateFound) {
            log.error("Following reading already exists for this account:");
            log.error(reading.toString());
            throw new DuplicateReadingFoundException(reading.getReadingType(), reading.getDate());
        }
    }

    public List<ReadingDto> convertReadingsToDto(ReadingType readingType, int accountId){

        final List<Reading> accountReadings = readingRepository.findAllByAccountIdAndReadingTypeOrderByDateAsc(accountId, readingType);
        final List<ReadingDto> readingDtos = new ArrayList<>();

        if(!accountReadings.isEmpty()) {
            accountReadings.stream().forEach(r -> readingDtos.add(convertToDto(r)));
        }
        return readingDtos;
    }
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public BigDecimal calculateCustomerAverageUsage(ReadingType readingType){
        List<Reading> orderedReadings = readingRepository.findAllByReadingTypeOrderByDateDesc(readingType);
        List<BigDecimal> result = orderedReadings.stream().filter(distinctByKey(Reading::getAccountId)).map(r -> r.getAvgDailyUsage()).collect(Collectors.toList());
        BigDecimal total = result.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(result.size()), 2, RoundingMode.HALF_EVEN);
        return average;
    }

    public ReadingDto convertToDto(Reading reading) {
        ReadingDto readingDto = modelMapper.map(reading, ReadingDto.class);
        return readingDto;
    }

    public Reading convertToEntity(ReadingDto readingDto, ReadingType readingType, int accountId) {
        Reading reading = modelMapper.map(readingDto, Reading.class);

        Reading lastReading = readingRepository.findFirstByAccountIdAndReadingTypeOrderByDateDesc(accountId, readingType);
        Reading firstReading = readingRepository.findFirstByAccountIdAndReadingTypeOrderByDateAsc(accountId, readingType);

        if(lastReading == null){
            lastReading = reading;
            firstReading = reading;
        }

        if(lastReading.getDate().isAfter(readingDto.getDate())) {
            log.error("Following reading is dated before the previous reading on this account:");
            log.error(reading.toString());
            throw new HistoricReadingFoundException(readingType, reading.getDate());
        }
        if(lastReading.getReading().compareTo(readingDto.getReading()) > 0) {
            log.error("Following reading's value is lower than the previous reading on this account:");
            log.error(reading.toString());
            throw new ReadingTooLowException(readingType, reading.getDate());
        }

        reading.setPeriodSinceLastRead(BigDecimal.valueOf(DAYS.between(lastReading.getDate(), readingDto.getDate())));
        reading.setUsageSinceLastRead(readingDto.getReading().subtract(lastReading.getReading()));

        BigDecimal elapsedSinceFirstRead = BigDecimal.valueOf(DAYS.between(firstReading.getDate(), reading.getDate()));
        BigInteger usageSinceFirstRead = reading.getReading().subtract(firstReading.getReading());

        BigDecimal averageDailyUsage = elapsedSinceFirstRead.compareTo(BigDecimal.ZERO.ZERO) > 0 ? new BigDecimal(usageSinceFirstRead).divide(elapsedSinceFirstRead, RoundingMode.HALF_EVEN): new BigDecimal(usageSinceFirstRead);

        reading.setAvgDailyUsage(averageDailyUsage);

        reading.setAccountId(accountId);
        reading.setReadingType(readingType);
        log.info(String.format("Created reading " + reading));
        return reading;
    }
}
