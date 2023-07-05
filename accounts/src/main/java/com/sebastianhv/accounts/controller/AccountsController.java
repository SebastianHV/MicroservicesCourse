package com.sebastianhv.accounts.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sebastianhv.accounts.config.AccountServiceConfig;
import com.sebastianhv.accounts.model.*;
import com.sebastianhv.accounts.repository.AccountsRepository;
import com.sebastianhv.accounts.service.client.CardsFeignClient;
import com.sebastianhv.accounts.service.client.LoansFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountsController {

    // We add a logger to test the Sleuth component
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    AccountServiceConfig accountsConfig;

    @Autowired
    LoansFeignClient loansFeignClient;

    @Autowired
    CardsFeignClient cardsFeignClient;


    @PostMapping("/myAccount")
    @Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
    public Accounts getAccountDetails(@RequestBody Customer customer) {

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        if (accounts != null) {
            return accounts;
        } else {
            return null;
        }
    }

    @GetMapping("/account/properties")
    public String getPropertyDetails() throws JsonProcessingException {
//        We use jackson API to convert the object to JSON format to be able to display it to the user into a JSON format
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Properties properties = new Properties(accountsConfig.getMsg(), accountsConfig.getBuildVersion(), accountsConfig.getMailDetails(), accountsConfig.getActiveBranches());
        String jsonStr = ow.writeValueAsString(properties);
        return jsonStr;
    }

//    We create a new method that will expose an API path with the customer details
    @PostMapping("/myCustomerDetails")
    @CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallback")
    @Retry(name = "retryForCustomerDetails", fallbackMethod = "myCustomerDetailsFallback")
//    We add the header as a parameter we can accept
    public CustomerDetails myCustomerDetails(@RequestHeader("eazybank-correlation-id") String correlationId, @RequestBody Customer customer) {
//        We add the logger
        logger.info("myCustomerDetails() method started.");
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
//        Using the feign client of loans and cards microservices, we can invoke those paths without knowing their exact ip and port, or direct endpoint URL
//        We update our methods to accept the Header correlationId
        List<Loans> loans = loansFeignClient.getLoansDetails(correlationId, customer);
        List<Cards> cards = cardsFeignClient.getCardDetails(correlationId, customer);

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loans);
        customerDetails.setCards(cards);
//        We add another logger
        logger.info("myCustomerDetails(method ended.");
        return customerDetails;
    }

//    It has to accept the same request parameters/object as the original method/API call
//    It has to accept a throwable, so we can make our fallback mechanism based on the exception we received from the original method
    private CustomerDetails myCustomerDetailsFallback(@RequestHeader("eazybank-correlation-id") String correlationId, Customer customer, Throwable t) {
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        List<Loans> loans = loansFeignClient.getLoansDetails(correlationId, customer);
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loans);
        return customerDetails;
    }

    @GetMapping("/sayHello")
    @RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallback")
    public String sayHello() {
        return "Hello, Welcome to EazyBank.";
    }

    private String sayHelloFallback(Throwable t) {
        return "Hi, Welcome to Eazybank.";
    }
}
