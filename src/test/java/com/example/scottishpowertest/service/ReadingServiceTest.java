package com.example.scottishpowertest.service;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.dto.ReadingDto;
import com.example.scottishpowertest.exception.DuplicateReadingFoundException;
import com.example.scottishpowertest.exception.HistoricReadingFoundException;
import com.example.scottishpowertest.exception.ReadingTooLowException;
import com.example.scottishpowertest.entities.Reading;
import com.example.scottishpowertest.repository.ReadingRepository;
import org.apache.logging.slf4j.SLF4JLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static java.time.temporal.ChronoUnit.DAYS;


@ExtendWith(MockitoExtension.class)
public class ReadingServiceTest {

    private final static Logger log = LoggerFactory.getLogger(SLF4JLogger.class);

    @InjectMocks
    private ReadingService readingService;
    @Mock
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private ReadingRepository readingRepository;

    Reading readingOne = new Reading(1, 123, new BigInteger("8456"), ReadingType.ELECTRIC, LocalDate.of(2023,2,6));
    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        readingOne.setAvgDailyUsage(new BigDecimal("25"));
        readingOne.setUsageSinceLastRead(BigInteger.ZERO);
        readingOne.setPeriodSinceLastRead(new BigDecimal("31"));
    }

    @Test
    public void validateReadingDuplicateFoundTest(){
        List<Reading> duplicateList = new ArrayList<Reading>();
        duplicateList.add(readingOne);
        doReturn(duplicateList).when(readingRepository).findAllByAccountIdAndReadingTypeOrderByDateAsc(1, ReadingType.ELECTRIC);

        DuplicateReadingFoundException e = Assert.assertThrows(DuplicateReadingFoundException.class,  () -> {
            readingService.validateReading(readingOne, 1);});

        assertEquals(String.format("Duplicate reading of type %s and date %s found for this account, see logs for more info",
                readingOne.getReadingType(),readingOne.getDate().toString()), e.getMessage());
    }

    @Test
    public void validateReadingValidTest(){
        Reading validReading = new Reading(1, 123, new BigInteger("8456"), ReadingType.ELECTRIC, LocalDate.of(2100, 1, 1));
        List<Reading> validList = new ArrayList<>();
        validList.add(validReading);
        doReturn(validList).when(readingRepository).findAllByAccountIdAndReadingTypeOrderByDateAsc(readingOne.getAccountId(), ReadingType.ELECTRIC);
        readingService.validateReading(readingOne, readingOne.getAccountId());
    }

    @Test
    public void covertReadingsToDtoNoReadingsTest(){
        doReturn(new ArrayList<>()).when(readingRepository).findAllByAccountIdAndReadingTypeOrderByDateAsc(readingOne.getAccountId(), ReadingType.ELECTRIC);
        List<ReadingDto> result = readingService.convertReadingsToDto(ReadingType.ELECTRIC, readingOne.getAccountId());
        assertEquals(0, result.size());
    }

    @Test
    public void covertReadingsToDtoSingleReadingFoundTest(){
        List<Reading> previousReading = new ArrayList<>();
        previousReading.add(readingOne);
        doReturn(previousReading).when(readingRepository).findAllByAccountIdAndReadingTypeOrderByDateAsc(readingOne.getAccountId(), ReadingType.ELECTRIC);
        List<ReadingDto> result = readingService.convertReadingsToDto(ReadingType.ELECTRIC, readingOne.getAccountId());
        assertEquals(1, result.size());
    }

    @Test
    public void covertReadingsToDtoMultipleReadingsFoundTest(){
        Reading readingTwo = new Reading(1, 123, new BigInteger("9000"), ReadingType.ELECTRIC, LocalDate.of(2024, 4, 11));
        Reading readingThree = new Reading(1, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2025, 2, 20));

        List<Reading> previousReading = new ArrayList<>();
        previousReading.add(readingOne);
        previousReading.add(readingTwo);
        previousReading.add(readingThree);

        doReturn(previousReading).when(readingRepository).findAllByAccountIdAndReadingTypeOrderByDateAsc(readingOne.getAccountId(), ReadingType.ELECTRIC);
        List<ReadingDto> result = readingService.convertReadingsToDto(ReadingType.ELECTRIC, readingOne.getAccountId());
        assertEquals(3, result.size());
    }

    @Test
    public void calculateAverageUsageTest(){
        Reading readingTwo = new Reading(2, 123, new BigInteger("9000"), ReadingType.ELECTRIC, LocalDate.of(2024, 4, 11));
        Reading readingThree = new Reading(3, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2025, 2, 20));
        Reading readingFour = new Reading(1, 123, new BigInteger("9000"), ReadingType.ELECTRIC, LocalDate.of(2022, 4, 11));
        Reading readingFive = new Reading(2, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2024, 4, 10));
        Reading readingSix = new Reading(3, 123, new BigInteger("9000"), ReadingType.ELECTRIC, LocalDate.of(2021, 4, 11));

        readingOne.setAvgDailyUsage(new BigDecimal("25"));
        readingTwo.setAvgDailyUsage(BigDecimal.ZERO);
        readingThree.setAvgDailyUsage(new BigDecimal("100"));
        readingFour.setAvgDailyUsage(new BigDecimal("1"));
        readingFive.setAvgDailyUsage(new BigDecimal("2"));
        readingSix.setAvgDailyUsage(new BigDecimal("3"));

        List<Reading> orderedCustomerReadings = new ArrayList<>();
        orderedCustomerReadings.add(readingOne);
        orderedCustomerReadings.add(readingTwo);
        orderedCustomerReadings.add(readingThree);
        orderedCustomerReadings.add(readingFour);
        orderedCustomerReadings.add(readingFive);
        orderedCustomerReadings.add(readingSix);

        doReturn(orderedCustomerReadings).when(readingRepository).findAllByReadingTypeOrderByDateDesc(ReadingType.ELECTRIC);

        BigDecimal result = readingService.calculateCustomerAverageUsage(ReadingType.ELECTRIC);
        assertEquals(new BigDecimal("41.67"), result);
    }

    @Test
    public void convertToEntityTest(){
        Reading readingTwo = new Reading(1, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2025, 2, 20));

        ReadingDto testReading = new ReadingDto();
        testReading.setMeterId(123);
        testReading.setDate(LocalDate.of(2026,1,1));
        testReading.setReading(new BigInteger("15000"));

        Reading mappedTestReading = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2026,1,1));

        doReturn(readingTwo).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateDesc(1, ReadingType.ELECTRIC);
        doReturn(readingOne).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateAsc(1, ReadingType.ELECTRIC);
        doReturn(mappedTestReading).when(modelMapper).map(testReading, Reading.class);

        BigDecimal period = BigDecimal.valueOf((DAYS.between(readingOne.getDate(), testReading.getDate())));
        BigDecimal usage = new BigDecimal(testReading.getReading().subtract(readingOne.getReading()));
        BigDecimal average = usage.divide(period, RoundingMode.HALF_EVEN);

        Reading expectedOutput = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2026,1,1));

        expectedOutput.setAvgDailyUsage(average);
        expectedOutput.setPeriodSinceLastRead(BigDecimal.valueOf(DAYS.between(readingTwo.getDate(), testReading.getDate())));
        expectedOutput.setUsageSinceLastRead(testReading.getReading().subtract(readingTwo.getReading()));

        Reading response = readingService.convertToEntity(testReading, ReadingType.ELECTRIC, 1);

        assertEquals(expectedOutput.getAvgDailyUsage(), response.getAvgDailyUsage());
        assertEquals(expectedOutput.getUsageSinceLastRead(), response.getUsageSinceLastRead());
        assertEquals(expectedOutput.getPeriodSinceLastRead(), response.getPeriodSinceLastRead());
    }

    @Test
    public void convertToEntityLastReadingNullTest(){
        ReadingDto testReading = new ReadingDto();
        testReading.setMeterId(123);
        testReading.setDate(LocalDate.of(2026,1,1));
        testReading.setReading(new BigInteger("15000"));

        Reading mappedTestReading = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2026,1,1));

        doReturn(null).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateDesc(1, ReadingType.ELECTRIC);
        doReturn(null).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateAsc(1, ReadingType.ELECTRIC);
        doReturn(mappedTestReading).when(modelMapper).map(testReading, Reading.class);

        Reading expectedOutput = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2026,1,1));

        expectedOutput.setAvgDailyUsage(BigDecimal.ZERO);
        expectedOutput.setPeriodSinceLastRead(BigDecimal.ZERO);
        expectedOutput.setUsageSinceLastRead(BigInteger.ZERO);

        Reading response = readingService.convertToEntity(testReading, ReadingType.ELECTRIC, 1);

        assertEquals(expectedOutput.getAvgDailyUsage(), response.getAvgDailyUsage());
        assertEquals(expectedOutput.getUsageSinceLastRead(), response.getUsageSinceLastRead());
        assertEquals(expectedOutput.getPeriodSinceLastRead(), response.getPeriodSinceLastRead());
    }

    @Test
    public void convertToEntityHistoricReadingTest(){
        Reading readingTwo = new Reading(1, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2025, 2, 20));

        ReadingDto testReading = new ReadingDto();
        testReading.setMeterId(123);
        testReading.setDate(LocalDate.of(2024,1,1));
        testReading.setReading(new BigInteger("15000"));

        Reading mappedTestReading = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2024,1,1));

        doReturn(readingTwo).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateDesc(1, ReadingType.ELECTRIC);
        doReturn(readingOne).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateAsc(1, ReadingType.ELECTRIC);
        doReturn(mappedTestReading).when(modelMapper).map(testReading, Reading.class);

        HistoricReadingFoundException e = Assert.assertThrows(HistoricReadingFoundException.class,  () -> {
            readingService.convertToEntity(testReading, ReadingType.ELECTRIC, 1);});

        assertEquals(String.format("Reading of type %s and date %s is dated before the previous reading on this account, see logs for more info",
                ReadingType.ELECTRIC,testReading.getDate().toString()), e.getMessage());
    }

    @Test
    public void convertToEntityReadingTooLowTest(){
        Reading readingTwo = new Reading(1, 123, new BigInteger("11120"), ReadingType.ELECTRIC, LocalDate.of(2025, 2, 20));

        ReadingDto testReading = new ReadingDto();
        testReading.setMeterId(123);
        testReading.setDate(LocalDate.of(2026,1,1));
        testReading.setReading(new BigInteger("100"));

        Reading mappedTestReading = new Reading(1, 123, new BigInteger("15000"), ReadingType.ELECTRIC, LocalDate.of(2026,1,1));

        doReturn(readingTwo).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateDesc(1, ReadingType.ELECTRIC);
        doReturn(readingOne).when(readingRepository).findFirstByAccountIdAndReadingTypeOrderByDateAsc(1, ReadingType.ELECTRIC);
        doReturn(mappedTestReading).when(modelMapper).map(testReading, Reading.class);

        ReadingTooLowException e = Assert.assertThrows(ReadingTooLowException.class,  () -> {
            readingService.convertToEntity(testReading, ReadingType.ELECTRIC, 1);});

        assertEquals(String.format("Reading of type %s and date %s is lower than the previous reading on this account, see logs for more info",
                ReadingType.ELECTRIC,testReading.getDate().toString()), e.getMessage());
    }

    @Test
    public void convertToDtoTest() {
        ReadingDto readingDtoOne = new ReadingDto();
        readingDtoOne.setReading(readingOne.getReading());
        readingDtoOne.setId(readingOne.getId());
        readingDtoOne.setMeterId(readingOne.getMeterId());
        readingDtoOne.setDate(readingOne.getDate());

        doReturn(readingDtoOne).when(modelMapper).map(readingOne, ReadingDto.class);

        ReadingDto serviceResponse = readingService.convertToDto(readingOne);

        assertEquals(readingDtoOne.getMeterId(), serviceResponse.getMeterId());
        assertEquals(readingDtoOne.getDate(), serviceResponse.getDate());
    }
}
