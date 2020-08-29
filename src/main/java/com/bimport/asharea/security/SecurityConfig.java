package com.bimport.asharea.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String[] AUTH_WHITELIST = {
            "/Login/**",
            // -- swagger ui
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/"
            // other public endpoints of your API may be appended to this array
    };

    private final EntryPointUnauthorizedHandler unauthorizedHandler;

    @Autowired
    public SecurityConfig(EntryPointUnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthenticationCookieFilter authenticationCookieFilterBean() throws Exception {
        AuthenticationCookieFilter authenticationCookieFilter = new AuthenticationCookieFilter();
        authenticationCookieFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationCookieFilter;
    }

    /**
     * AuthenticationCookieFilter needs to be called before authenticated(), and should not be auto-registered
     * later again by Spring Boot
     */
    @Bean
    public FilterRegistrationBean authenticationCookieFilterRegistration(AuthenticationCookieFilter filter) {
        FilterRegistrationBean<?> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilter(authenticationCookieFilterBean())
                .authorizeRequests().antMatchers("/api/**").authenticated().anyRequest().permitAll()
                .and().exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler)
                .and().httpBasic()
                .and().csrf().disable();

        http.headers().frameOptions().sameOrigin();
        //http.addFilterBefore(characterEncodingFilter(), AuthenticationCookieFilter.class);
        //http.addFilter(new MyCorsFilter());
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(AUTH_WHITELIST);
    }

    /* Problem: CorsConfigurationSource not work*/

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
//        configuration.setAllowCredentials(true);
//        configuration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Headers","Access-Control-Allow-Origin","Access-Control-Request-Method", "Access-Control-Request-Headers","Origin","Cache-Control", "Content-Type", "Authorization"));
//        configuration.setAllowedMethods(Arrays.asList("DELETE", "GET", "POST", "PATCH", "PUT"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

}
