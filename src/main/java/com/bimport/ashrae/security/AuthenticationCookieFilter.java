package com.bimport.ashrae.security;

import com.bimport.ashrae.security.UserDetail.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationCookieFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private CustomUserDetailService userDetailsService;

    @Autowired
    private LoggedInValidator loggedInValidator;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String method = ((HttpServletRequest)req).getMethod();
        if (method.equalsIgnoreCase("OPTIONS")) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(null, null, null));
            chain.doFilter(req, res);
            return;
        }

        String userId = loggedInValidator.validate((HttpServletRequest) req);
        if (userId != null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        req.setAttribute("userId", userId);
        logger.info("Cookie has been authenticated for user: " + userId);

        chain.doFilter(req, res);
    }
}
