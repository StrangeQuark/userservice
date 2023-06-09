package com.strangequark.userservice.auth;

import com.strangequark.userservice.config.JwtService;
import com.strangequark.userservice.user.Role;
import com.strangequark.userservice.user.User;
import com.strangequark.userservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * {@link Service} for registering and authenticating user requests
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    /**
     * {@link UserRepository} for fetching {@link com.strangequark.userservice.user.User} from the database
     */
    private final UserRepository userRepository;

    /**
     * {@link PasswordEncoder} for encoding our password when registering a new user to the database
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * {@link JwtService} for generating a JWT token to return with the registration response
     */
    private final JwtService jwtService;

    /**
     * {@link AuthenticationManager} for authenticating JWT tokens
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Business logic for registering a new user
     * @param registrationRequest
     * @return {@link AuthenticationResponse} with a generated JWT token
     */
    public AuthenticationResponse register(RegistrationRequest registrationRequest) {

        //Build the user object to be saved to the database
        User user = User.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))//Encode the password before saving to database
                .role(Role.USER)
                .build();

        //Save the user to the database
        userRepository.save(user);

        //Create a JWT token to return with the response
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .build();
    }

    /**
     * Business logic for authenticating a user
     * @param authenticationRequest
     * @return {@link AuthenticationResponse}
     */
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {

        //Authenticate the user, throw an exception if the username and password combination are incorrect
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(),
                authenticationRequest.getPassword()
            )
        );

        //Get the user, throw an exception if the username is not found
        User user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        //Create a JWT token to authenticate the user
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .build();
    }
}
