package com.banking.app.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtAuthenthicationFilter jwtAuthenthicationFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return  configuration.getAuthenticationManager();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider= new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
       // httpSecurity.csrf(csrf->csrf.disable())-----lamda referece
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize->authorize
                        .requestMatchers(HttpMethod.POST,"/api/user").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/user/login").permitAll()
//                        .requestMatchers(HttpMethod.POST,"api/user/credit").permitAll()
//                        .requestMatchers(HttpMethod.POST,"api/user/debit").permitAll()
//                        .requestMatchers(HttpMethod.POST,"api/user/transfer").permitAll()
//                        .requestMatchers(HttpMethod.GET,"api/user/nameEnquiry").permitAll()
//                        .requestMatchers(HttpMethod.GET,"api/user/balanceEnquiry").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/bankstatement/**").permitAll()
//                        .requestMatchers(HttpMethod.DELETE,"/bankstatement/**").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/bankstatement/all").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/bankstatement/search/{}").permitAll()
                        .anyRequest().authenticated());
        httpSecurity.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.authenticationProvider(authenticationProvider());
        httpSecurity.addFilterBefore(jwtAuthenthicationFilter, UsernamePasswordAuthenticationFilter.class);
        return  httpSecurity.build();

    }

}
