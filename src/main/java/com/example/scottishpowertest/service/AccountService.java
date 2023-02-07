package com.example.scottishpowertest.service;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.dto.AccountDto;
import com.example.scottishpowertest.dto.ReadingDto;
import com.example.scottishpowertest.exception.AccountNotFoundException;
import com.example.scottishpowertest.entities.Account;
import com.example.scottishpowertest.repository.AccountRepository;
import org.apache.logging.slf4j.SLF4JLogger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountService {


    private final ModelMapper modelMapper = new ModelMapper();
    private final static Logger log = LoggerFactory.getLogger(SLF4JLogger.class);

    private final ReadingService readingService;

    private final AccountRepository accountRepository;

    AccountService(final AccountRepository accountRepository, final ReadingService readingService) {
        this.accountRepository = accountRepository;
        this.readingService = readingService;
    };

    final public Account getAccount(final int accountNumber){
        final Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> {
                    log.error(String.format("Account with id: %o not in the database", accountNumber));
                    return new AccountNotFoundException(accountNumber);
                });

        log.info(String.format("Found account with id: %o in the database", accountNumber));
        return account;
    }

    final public AccountDto getAccountReadings(final int accountNumber){
        final Account account = getAccount(accountNumber);
        final AccountDto accountDto = convertToDto(account);
        return accountDto;
    }

    final public void saveReadingsToAccount(final AccountDto accountReadings){

        getAccount(accountReadings.getAccountId());

        final List<ReadingDto> gasReadings = accountReadings.getGasReadings();
        final List<ReadingDto> elecReadings = accountReadings.getElecReadings();

        Optional.ofNullable(gasReadings).orElse(Collections.emptyList()).stream()
                .forEach(r -> readingService.saveReading(r, ReadingType.GAS, accountReadings.getAccountId()));
        Optional.ofNullable(elecReadings).orElse(Collections.emptyList()).stream()
                .forEach(r -> readingService.saveReading(r, ReadingType.ELECTRIC, accountReadings.getAccountId()));
    }

    final public AccountDto convertToDto(final Account account) {
        final AccountDto accountDto = modelMapper.map(account, AccountDto.class);
        accountDto.setElecReadings(readingService.convertReadingsToDto(ReadingType.ELECTRIC, account.getAccountId()));
        accountDto.setGasReadings(readingService.convertReadingsToDto(ReadingType.GAS, account.getAccountId()));
        accountDto.setGasComparison(readingService.calculateCustomerAverageUsage(ReadingType.GAS));
        accountDto.setElecComparison(readingService.calculateCustomerAverageUsage(ReadingType.ELECTRIC));
        return accountDto;
    }
}
