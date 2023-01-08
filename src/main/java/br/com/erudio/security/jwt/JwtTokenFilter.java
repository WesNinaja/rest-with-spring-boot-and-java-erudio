package br.com.erudio.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtTokenFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //Esse filtro vai ser executado a cada requisição. Nesse caso abaixo está obtendo o token a partir da request
        String token = tokenProvider.resolveToken((HttpServletRequest) request);
        //Depois de obter o token, ele valida o token
        if (token != null && tokenProvider.validateToken(token)){
            //Depois que ele valida, ele obtem uma autenticação
            Authentication auth = tokenProvider.getAuthentication(token);
            if (auth != null) {
                //Se ele conseguir obter autenticação, então ele seta essa autenticação na sessão do spring, no securityContextHolder do spring
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
