package com.sebastianhv.accounts.service.client;

import com.sebastianhv.accounts.model.Cards;
import com.sebastianhv.accounts.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

//Here, we have to give the logical name of the service that is registered in the Eureka server
//In this case, the logical name for our Cards service is "cards"
@FeignClient("cards")
public interface CardsFeignClient {

//    Here, we give the value of the URL path of the method I want to invoke
//    We can check the myCards method in the Cards mService Controller Class to check what kind ok method requires and what type/format consumes
    @RequestMapping(method = RequestMethod.POST, value = "myCards", consumes = "application/json")
    List<Cards> getCardDetails(@RequestBody Customer customer);
}
