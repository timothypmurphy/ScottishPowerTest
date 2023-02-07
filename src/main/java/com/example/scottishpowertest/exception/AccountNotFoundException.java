package com.example.scottishpowertest.exception;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(int id){
        super(String.format("Problem getting account with id %o, please access application logs for more info", id));
    }
}
