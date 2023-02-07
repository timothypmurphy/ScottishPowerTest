package com.example.scottishpowertest.controller;

import com.example.scottishpowertest.dto.AccountDto;
import com.example.scottishpowertest.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/smart/reads")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/{accountNumber}")
    final AccountDto getAccount(@PathVariable final int accountNumber){

        return service.getAccountReadings(accountNumber);
    }

    @PostMapping()
    final ResponseEntity<String> addReading(@Valid @RequestBody final AccountDto readings){
        log.info(String.format("Received readings " + readings));


        service.saveReadingsToAccount(readings);
        return ResponseEntity.ok("Readings are valid");
    }
}
