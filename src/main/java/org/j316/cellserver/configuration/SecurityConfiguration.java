package org.j316.cellserver.configuration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${adapter.security.username}")
  private String username;
  @Value("${adapter.security.password}")
  private String password;

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic();

    return http.build();
  }


  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    List<UserDetails> userDetailsList = new ArrayList<>();
    userDetailsList.add(
        User.withUsername(username).password(passwordEncoder().encode(password))
            .roles("USER").build());
    return new InMemoryUserDetailsManager(userDetailsList);
  }

}
