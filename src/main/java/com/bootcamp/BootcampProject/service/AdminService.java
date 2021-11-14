package com.bootcamp.BootcampProject.service;

import com.bootcamp.BootcampProject.entity.user.User;
import com.bootcamp.BootcampProject.exception.UserNotFoundException;
import com.bootcamp.BootcampProject.repository.CustomerRepository;
import com.bootcamp.BootcampProject.repository.SellerRepository;
import com.bootcamp.BootcampProject.repository.UserRepository;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailSendService emailSendService;


    @Transactional
    public String activateUser(UUID userId) throws UserNotFoundException {
        if(userRepository.findById(userId).isPresent()){
            User user1 =userRepository.findById(userId).get();
            if (!user1.isActive()){
                user1.setActive(true);
                userRepository.save(user1);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user1.getEmail());
                message.setFrom("gunjansharma9876543210@gmail.com");
                message.setSubject("Account Activated");
                message.setText("Your account is successfully activated");
                emailSendService.sendEmail(message);
                return "User Activated";
            }
            else {
                return "Account is already activated";
            }
        }
        else {
            throw new UserNotFoundException("Incorrect user id");
        }
    }

    @Transactional
    public String deactivateUser(UUID userId) throws UserNotFoundException {
        if(userRepository.findById(userId).isPresent()){
            User user1 =userRepository.findById(userId).get();
            if (user1.isActive()){
                user1.setActive(false);
                userRepository.save(user1);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user1.getEmail());
                message.setFrom("gunjansharma9876543210@gmail.com");
                message.setSubject("Account Deactivated");
                message.setText("Your account is deactivated by admin");
                emailSendService.sendEmail(message);
                return "User Deactivated";
            }
            else {
                return "Account is already deactivated";
            }
        }
        else {
            throw new UserNotFoundException("Incorrect user id");
        }
    }

    public MappingJacksonValue findAllCustomer() {
        SimpleBeanPropertyFilter filterUser = SimpleBeanPropertyFilter.filterOutAllExcept("id","firstName","middleName","lastName","email","isActive");
        SimpleBeanPropertyFilter filterCustomer = SimpleBeanPropertyFilter.filterOutAllExcept("userId");
        FilterProvider filters = new SimpleFilterProvider().addFilter("customerFilter",filterCustomer).addFilter("userFilter",filterUser);
        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(customerRepository.findAll());
        mappingJacksonValue.setFilters(filters);
        return mappingJacksonValue;
    }



    public MappingJacksonValue findAllSeller() {
        SimpleBeanPropertyFilter filterUser = SimpleBeanPropertyFilter.filterOutAllExcept("id","firstName","lastName","email","isActive","addresses");
        SimpleBeanPropertyFilter filterAddress = SimpleBeanPropertyFilter.filterOutAllExcept("addressLine","city","state","country","zipcode");
        SimpleBeanPropertyFilter filterSeller = SimpleBeanPropertyFilter.filterOutAllExcept("companyName","companyContactNo","userId");
        FilterProvider filters = new SimpleFilterProvider().addFilter("userFilter",filterUser).addFilter("addressFilter",filterAddress).addFilter("sellerFilter",filterSeller);
        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(sellerRepository.findAll());
        mappingJacksonValue.setFilters(filters);
        return mappingJacksonValue;
    }
}