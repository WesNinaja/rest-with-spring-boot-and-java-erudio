package br.com.erudio.service;

import br.com.erudio.data.vo.v1.security.AccountCredentialsVO;
import br.com.erudio.data.vo.v1.security.TokenVO;
import br.com.erudio.repository.UserRepository;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    public ResponseEntity signin(AccountCredentialsVO data) {
        try {
            //Recebemos um AccountCredentialsVO e aí extraímos o usuário e a senha
            var username = data.getUsername();
            var password = data.getPassword();
            //Aí invocamos o autenticationManager e tentamos fazer o login desses caras
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            //Depois que fazemos isso, acessamos o repositório e buscamos pelo username
            var user = repository.findByUsername(username);
            //Se essa busca retornar algum usuário e for diferente de null, então ele vai criar o AcessToken passando o username e as permissões ou roles dele, caso contrário ele vai lançar uma exceção
            //Em caso de sucesso ele vai subir um ResponseEntity com o tokenResponse, e caso ocorra um erro, ele vai subir uma ResponseEntity com o tokenVO vazio
            var tokenResponse = new TokenVO();
            if (user != null) {
                tokenResponse = tokenProvider.createAccessToken(username, user.getRoles());
            } else {
                throw new UsernameNotFoundException("Username " + username + "not found!");
            }

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username/password supplied!");
        }
    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity refreshToken(String username, String refreshToken) {
        var user = repository.findByUsername(username);

        var tokenResponse = new TokenVO();
        if (user != null) {
            tokenResponse = tokenProvider.refreshToken(refreshToken);
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }
        return ResponseEntity.ok(tokenResponse);
    }
}
