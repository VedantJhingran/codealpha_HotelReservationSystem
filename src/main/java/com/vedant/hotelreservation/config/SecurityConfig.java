package com.vedant.hotelreservation.config;

import com.vedant.hotelreservation.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.context.*;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

	@Bean
	UserDetailsService userDetailsService(UserRepository users) {
		return email -> users.findByEmailIgnoreCase(email)
				.map(user -> User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles(user.getRole().name()).build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
				new HttpSessionSecurityContextRepository());
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository) throws Exception {
		CookieCsrfTokenRepository csrfTokens = CookieCsrfTokenRepository.withHttpOnlyFalse();
		http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
				.securityContext(context -> context.securityContextRepository(repository))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/home", "/rooms", "/rooms/**", "/login", "/register", "/bookings",
								"/admin", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/rooms/availability").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.httpBasic(basic -> basic.disable());
		return http.build();
	}
}
