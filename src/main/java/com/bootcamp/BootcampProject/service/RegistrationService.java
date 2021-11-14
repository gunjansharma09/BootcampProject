package com.bootcamp.BootcampProject.service;

import com.bootcamp.BootcampProject.dto.request.CustomerRegister;
import com.bootcamp.BootcampProject.dto.request.ResendToken;
import com.bootcamp.BootcampProject.dto.request.SellerRegister;
import com.bootcamp.BootcampProject.entity.token.ConfirmationToken;
import com.bootcamp.BootcampProject.entity.user.*;
import com.bootcamp.BootcampProject.exception.AlreadyExistException;
import com.bootcamp.BootcampProject.exception.TokenExpiredException;
import com.bootcamp.BootcampProject.repository.ConfirmationTokenRepository;
import com.bootcamp.BootcampProject.repository.CustomerRepository;
import com.bootcamp.BootcampProject.repository.SellerRepository;
import com.bootcamp.BootcampProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Service
public class RegistrationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailSendService emailSendService;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public Customer createNewCustomer(CustomerRegister customerRegister) throws AlreadyExistException {
        User userExists = userRepository.findByEmail(customerRegister.getEmail());
        if (userExists != null) throw new AlreadyExistException("User is already registered with the given email");
        else {
                Customer newCustomer = new Customer();
                User newUser = new User();
                newUser.setEmail(customerRegister.getEmail());
                newUser.setFirstName(customerRegister.getFirstName());
                newUser.setMiddleName(customerRegister.getMiddleName());
                newUser.setLastName(customerRegister.getLastName());
                newUser.setPassword(bCryptPasswordEncoder.encode(customerRegister.getPassword()));
                Address address = new Address();
                address.setAddressLine(customerRegister.getAddressLine());
                address.setCity(customerRegister.getCity());
                address.setCountry(customerRegister.getCountry());
                address.setState(customerRegister.getState());
                address.setZipcode(customerRegister.getZipcode());
                address.setLabel(customerRegister.getLabel());
                newUser.addAddresses(address);
                newUser.setActive(false);
                newUser.setNotDeleted(true);
                newUser.setNotLocked(true);
                newUser.setLoginAttempts(0);
                Role role = new Role();
                role.setAuthority("ROLE_CUSTOMER");
                newUser.setRoles(new ArrayList<>(Arrays.asList(role)));
                newCustomer.setContactNo(customerRegister.getContact());
                newCustomer.setUserId(newUser);
                customerRepository.save(newCustomer);
                ConfirmationToken confirmationToken= new ConfirmationToken(newUser);
                confirmationTokenRepository.save(confirmationToken);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(customerRegister.getEmail());
                message.setFrom("gunjansharma9876543210@gmail.com");
                message.setSubject("Complete Registration");
                message.setText("To confirm your account, please click here:"+"http://localhost:8080/register/confirm-account?token="+confirmationToken.getConfirmationToken());
                emailSendService.sendEmail(message);
                return newCustomer;
        }

    }

    public Seller createNewSeller(SellerRegister sellerRegister) throws AlreadyExistException {

              User userExists = userRepository.findByEmail(sellerRegister.getEmail());
        if (userExists != null) throw new AlreadyExistException("User is already registered with the given email");
        else if(sellerRepository.findByCompanyName(sellerRegister.getCompanyName())!=null){
            throw new AlreadyExistException("THe company name already exist under another seller name. PLease recheck.");
        }
        else {
            Seller newSeller = new Seller();
            User newUser = new User();
            newUser.setEmail(sellerRegister.getEmail());
            newUser.setFirstName(sellerRegister.getFirstName());
            newUser.setMiddleName(sellerRegister.getMiddleName());
            newUser.setLastName(sellerRegister.getLastName());
            newUser.setPassword(bCryptPasswordEncoder.encode(sellerRegister.getPassword()));
            Address address = new Address();
            address.setAddressLine(sellerRegister.getAddressLine());
            address.setCity(sellerRegister.getCity());
            address.setCountry(sellerRegister.getCountry());
            address.setState(sellerRegister.getState());
            address.setZipcode(sellerRegister.getZipcode());
            address.setLabel(sellerRegister.getLabel());
            address.setDelete(false);
            newUser.addAddresses(address);
            newUser.setActive(false);
            newUser.setNotDeleted(true);
            newUser.setNotLocked(true);
            newUser.setLoginAttempts(0);
            Role role = new Role();
            role.setAuthority("ROLE_SELLER");
            newUser.setRoles(new ArrayList<>(Arrays.asList(role)));
            newSeller.setCompanyContactNo(sellerRegister.getCompanyContactNo());
            newSeller.setCompanyName(sellerRegister.getCompanyName());
            newSeller.setGst(sellerRegister.getGst());
            newSeller.setUserId(newUser);
            sellerRepository.save(newSeller);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(sellerRegister.getEmail());
            message.setFrom("gunjansharma9876543210@gmail.com");
            message.setSubject("Waiting for activation!!");
            message.setText("Account has been created waiting for approval from admin");
            emailSendService.sendEmail(message);
            return newSeller;
        }
    }

    @Transactional
    public String confirmCustomerAccount(String confirmationToken) throws TokenExpiredException {
        ConfirmationToken confirmationToken1 = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
        if(confirmationToken1!=null) {
            Date presentDate = new Date();
            if (confirmationToken1.getExpiryDate().getTime() - presentDate.getTime() <= 0) {
                User userExists = confirmationToken1.getUser();
                confirmationTokenRepository.deleteConfirmationToken(confirmationToken);
                ConfirmationToken confirmationToken2 = new ConfirmationToken(userExists);
                confirmationTokenRepository.save(confirmationToken2);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(userExists.getEmail());
                message.setFrom("gunjansharma9876543210@gmail.com");
                message.setSubject("Complete Registration");
                message.setText("To confirm your account, please click here:"+"http://localhost:8080/confirm-account?token="+confirmationToken2.getConfirmationToken());
                emailSendService.sendEmail(message);
                throw new TokenExpiredException("Token has been expired!! New activation link send on your email");
            } else {
                User user = userRepository.findByEmail(confirmationToken1.getUser().getEmail());
                user.setActive(true);
                confirmationTokenRepository.deleteConfirmationToken(confirmationToken);
                return "ThankYou , Your account has been verified";
            }
        }
        else {
            return "Error! Please try again";
        }
    }

    @Transactional
    public String resendActivationToken(ResendToken email){
        System.out.println(email);
        User userExists = userRepository.findByEmail(email.getEmail());
        if (userExists != null){
            if(!userExists.isActive()){
                ConfirmationToken confirmationToken = null;
                confirmationToken = confirmationTokenRepository.findByUser(userExists);

                if (confirmationToken!=null){
                    String token = confirmationToken.getConfirmationToken();
                    Date presentDate= new Date();
                    if(confirmationToken.getExpiryDate().getTime() - presentDate.getTime() <=0){
                        confirmationTokenRepository.deleteConfirmationToken(token);

                        ConfirmationToken confirmationToken1 = new ConfirmationToken(userExists);
                        confirmationTokenRepository.save(confirmationToken1);

                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(userExists.getEmail());
                        message.setFrom("gunjansharma9876543210@gmail.com");
                        message.setSubject("Complete Registration");
                        message.setText("To confirm your account, please lick here:"+"http://localhost:8080/confirm-account?token="+confirmationToken1.getConfirmationToken());
                        emailSendService.sendEmail(message);

                        return "New activation link is successfully sent on your registered email";
                    }
                    else {
                        return "Your current activation link is not expired yet,please use the already existing link to activate";
                    }
                }
                else {
                    ConfirmationToken newConfirmationToken =new ConfirmationToken(userExists);
                    confirmationTokenRepository.save(newConfirmationToken);

                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(userExists.getEmail());
                    message.setFrom("gunjansharma9876543210@gmail.com");
                    message.setSubject("Complete Registration");
                    message.setText("To confirm your account, please click here:"+"http://localhost:8080/confirm-account?token="+newConfirmationToken.getConfirmationToken());
                    emailSendService.sendEmail(message);

                    return "New activation link is successfully sent on your registered email";
                }
            }
            else {
                return "Account is already active. No need to generate an activation token";
            }
        }
        else {
            throw new UsernameNotFoundException("Invalid email entered");
        }
    }
}
