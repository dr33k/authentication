package com.seven.auth.user;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@ApplicationScope
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Authentication userAuthentication;

    public UserService(UserRepository userRepository ,
                       BCryptPasswordEncoder passwordEncoder ,
                       Authentication userAuthentication) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
        this.userAuthentication = userAuthentication;
    }

    //For Admin
    public Set <UserRecord> getAll() {
        List <User> userList = userRepository.findAll();

        Set <UserRecord> userRecords =
                userList.stream().map(UserRecord::copy).collect(Collectors.toSet());

        return userRecords;
    }

    public UserRecord get(UUID id) {
        User userFromDb;
         //Signifies account owner access.
            userFromDb = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND ,
                            "This user does not exist or has been deleted"));

        return UserRecord.copy(userFromDb);
    }

    public UserRecord create(UserCreateRequest userCreateRequest) {
        try {
            if (userRepository.existsByEmail(userCreateRequest.getEmail()))
                throw new ResponseStatusException(HttpStatus.CONFLICT , "A user with this email already exists");

            User user = new User();
            BeanUtils.copyProperties(userCreateRequest , user);

            //Set role
            user.setRole(UserRole.PASSENGER);

            //Encode password
            user.setPassword(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()));
            //Save
            userRepository.save(user);

            return UserRecord.copy(user);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,
                    "User could not be created, please try again later. Why? " + ex.getMessage());
        }
    }

    //For User
    public void delete(UUID id) {//Only the user can deactivate their account
        User user = (User) userAuthentication.getPrincipal();
        if (user.getId() != id) throw new ResponseStatusException(HttpStatus.FORBIDDEN , "Account Breach");

        userRepository.deleteById(id);
    }

    //For User
    public UserRecord update(UUID id, UserUpdateRequest userUpdateRequest) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User account could not be found"));

            Boolean modified = false;

            //If the property is not null
            if (userUpdateRequest.getFirstName() != null) {
                user.setFirstName(userUpdateRequest.getFirstName());
                modified = true;
            }
            if (userUpdateRequest.getLastName() != null) {
                user.setLastName(userUpdateRequest.getLastName());
                modified = true;
            }
            if (userUpdateRequest.getPassword() != null) {
                user.setPassword(userUpdateRequest.getPassword());
                modified = true;
            }
            if (userUpdateRequest.getPhoneNo() != null) {
                user.setPhoneNo(userUpdateRequest.getPhoneNo());
                modified = true;
            }
            if (userUpdateRequest.getDateBirth() != null) {
                user.setDob(userUpdateRequest.getDateBirth());
                modified = true;
            }
            if (modified) userRepository.save(user);

            return UserRecord.copy(user);

        }catch (ResponseStatusException ex) {throw ex;}
        catch (Exception ex) {
            throw new RuntimeException("User could not be modified, please contact System Administrator. Why? " + ex.getMessage());
        }
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}