package com.example.scottishpowertest.service;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.dto.AccountDto;
import com.example.scottishpowertest.dto.ReadingDto;
import com.example.scottishpowertest.entities.Account;
import com.example.scottishpowertest.entities.Reading;
import com.example.scottishpowertest.repository.AccountRepository;
import com.example.scottishpowertest.repository.ReadingRepository;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;
    @Mock
    private ReadingService readingService;
    @Mock
    private AccountRepository accountRepository;

    private ModelMapper modelMapper = new ModelMapper();

    List<Reading> readings = new ArrayList<Reading>();

    Reading readingOne = new Reading(1, 123, new BigInteger("5456"), ReadingType.ELECTRIC, LocalDate.now());
    Reading readingTwo = new Reading(1, 456, new BigInteger("6456"), ReadingType.GAS, LocalDate.now());
    Reading readingThree = new Reading(1, 789, new BigInteger("7456"), ReadingType.ELECTRIC, LocalDate.now());
    Account accountOne = new Account();


    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        readings.clear();
    }

    @Test
    public void getAccountTest(){
        readings.add(readingOne);
        readings.add(readingTwo);
        readings.add(readingThree);
        accountOne.setAccountId(1);
        accountOne.setReadings(readings);

        doReturn(Optional.of(accountOne)).when(accountRepository).findById(1);

        Account testAccount = accountService.getAccount(1);

        assertEquals(readings, testAccount.getReadings());
        assertEquals(1, testAccount.getAccountId());
    }

    @Test
    public void convertToDtoTest() {
        readings.add(readingOne);
        readings.add(readingTwo);
        readings.add(readingThree);
        accountOne.setAccountId(1);
        accountOne.setReadings(readings);

        ReadingDto readingDtoOne = modelMapper.map(readingOne, ReadingDto.class);
        ReadingDto readingDtoTwo = modelMapper.map(readingTwo, ReadingDto.class);
        ReadingDto readingDtoThree = modelMapper.map(readingThree, ReadingDto.class);

        List<ReadingDto> gasDtos = new ArrayList<>();
        List<ReadingDto> electricDtos = new ArrayList<>();

        gasDtos.add(readingDtoTwo);
        electricDtos.add(readingDtoOne);
        electricDtos.add(readingDtoThree);

        doReturn(gasDtos).when(readingService).convertReadingsToDto(ReadingType.GAS, 1);
        doReturn(electricDtos).when(readingService).convertReadingsToDto(ReadingType.ELECTRIC, 1);

        AccountDto serviceResponse = accountService.convertToDto(accountOne);

        List<ReadingDto> elecReadings = new ArrayList<>();
        List<ReadingDto> gasReadings = new ArrayList<>();

        elecReadings.add(readingDtoOne);
        elecReadings.add(readingDtoThree);
        gasReadings.add(readingDtoTwo);

        assertEquals(1, serviceResponse.getAccountId());
        assertEquals(gasReadings, serviceResponse.getGasReadings());
        assertEquals(elecReadings, serviceResponse.getElecReadings());
    }
}
