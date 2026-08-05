package br.com.fiapx.auth.service;

import br.com.fiapx.auth.domain.User;
import br.com.fiapx.auth.dto.AuthResponseDTO;
import br.com.fiapx.auth.dto.LoginRequestDTO;
import br.com.fiapx.auth.dto.RegisterRequestDTO;
import br.com.fiapx.auth.dto.UserResponseDTO;
import br.com.fiapx.auth.repository.UserRepository;
import br.com.fiapx.auth.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public UserResponseDTO register(RegisterRequestDTO request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new DuplicateEmailException("Email already registered");
        });

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenService.generateToken(user);
        return new AuthResponseDTO(token, "Bearer", jwtTokenService.getExpirationInSeconds());
    }
}
