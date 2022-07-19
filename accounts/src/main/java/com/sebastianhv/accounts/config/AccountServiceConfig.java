package com.sebastianhv.accounts.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/*
* @author SebastianHV
*
*/
@Configuration
//Load all the properties that I need from the given properties file
//We will read all the properties with the prefix value accounts (eg. accounts.build-version)
@ConfigurationProperties(prefix = "accounts")
@Getter @Setter @ToString
public class AccountServiceConfig {
//    The data types and names match with the properties file values and details
    private String msg;
    private String buildVersion;
    private Map<String, String> mailDetails;
    private List<String> activeBranches;

}
