package org.j316.cellserver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${adapter.security.username}")
  private String username;

  @Value("${adapter.security.password}")
  private String password;

  @Value("${adapter.security.oauth2.enabled:false}")
  private boolean oauth2Enabled;

  @Value("${spring.security.oauth2.client.provider.okta.issuer-uri:}")
  private String oauth2IssuerUri;

  @Value("${spring.security.oauth2.client.registration.okta.client-id:}")
  private String oauth2ClientId;

  @Value("${spring.security.oauth2.client.registration.okta.client-secret:}")
  private String oauth2ClientSecret;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());

    if (oauth2LoginConfigured()) {
      http.oauth2Login(Customizer.withDefaults());
    }

    return http.build();
  }

  boolean oauth2LoginConfigured() {
    return oauth2Enabled
        && StringUtils.hasText(oauth2IssuerUri)
        && StringUtils.hasText(oauth2ClientId)
        && StringUtils.hasText(oauth2ClientSecret);
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
    UserDetails userDetails = User.withUsername(username)
        .password(passwordEncoder.encode(password))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(userDetails);
  }
}
