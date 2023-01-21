package br.com.erudio.security.jwt;

import br.com.erudio.data.vo.v1.security.TokenVO;
import br.com.erudio.exception.InvalidJwtAuthenticationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Base64;
import java.util.Date;
import java.util.List;


@Service
public class JwtTokenProvider {

    //Já seto o valor de default caso não tenha no application.yml
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey = "secret";
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000; //1 hour

    @Autowired
    private UserDetailsService userDetailsService;

    Algorithm algorithm = null;

    /**
     * Quando o spring inicia o contexto do spring, ou o spring context, que contem todos os beans, ele cria instancias dos beans anotados ou declarados na configuração,
     * processas as annotations, injeta as dependencias e faz mais outras coisas. Após ele inicializar corretamente tudo, ele chama os methods que estejam com o @PostConstruct
     * Na verdade, quando a instancia é criada, não há nada injetado ou inicalizado, portanto, o spring não vai conseguir injetar nenhuma dependencia antes de instanciar a class.
     * Portanto, em qualquer framework não é possível injetar a dependencia ou fazer qualquer outra coisa na class antes de chamar algum construtor.
     * A solução é usar um PostConstruct que permite executar uma ação logo após a inicialização do spring, porém antes do sistema executar alguma ação do usuário.
     * Ele é similar ao BeforeAll, que usamos nos testes automatizados.
     */
    @PostConstruct
    protected void init() {
        //Na prática ele pega o que a gente setou na secret e encripta ela, e seta novamente no valor dela, mudando esse valor.
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        // o argoritmo ele recebe a secret encriptada, o tipo de algoritmo que usamos é HMAC256
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }

    public TokenVO createAccessToken(String username, List<String> roles) {
        Date now = new Date();
        //Ele vai pegar a var now e vai somar mais uma hora. Ou seja, validity é o momento para daqui a uma hora.
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        //Agora vamos criar o access token e o refresh token
        var accessToken = getAccessToken(username, roles, now, validity);
        var refreshToken = getRegreshToken(username, roles, now);
        return new TokenVO(username, true, now, validity, accessToken, refreshToken);
    }

    public TokenVO refreshToken(String refreshToken) {
        if (refreshToken.contains("Bearer ")) refreshToken =
                refreshToken.substring("Bearer ".length());

        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);
        String username = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        return createAccessToken(username, roles);
    }

    private String getAccessToken(String username, List<String> roles, Date now, Date validity) {
        //Vamos setar a url de onde esse token foi setado. Nesse caso, vamos pegar a url do servidor.
        String issuerUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath().build().toUriString();

        //Agora vamos retornar um JWT
        return JWT.create()
                .withClaim("roles", roles)
                //Agora vamos passar quando esse token foi gerado
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(username)
                .withIssuer(issuerUrl)
                .sign(algorithm)
                .strip();
    }

    private String getRegreshToken(String username, List<String> roles, Date now) {
        Date validityRegreshToken = new Date(now.getTime() + validityInMilliseconds * 3); // 3 horas

        return JWT.create()
                .withClaim("roles", roles)
                //Agora vamos passar quando esse token foi gerado, e também vamos recalcular o validity
                .withIssuedAt(now)
                .withExpiresAt(validityRegreshToken)
                .withSubject(username)
                .sign(algorithm)
                .strip();
    }

    //Agora vamos implementar a parte de autenticação
    public Authentication getAuthentication(String token) {
        //Vamos decodificar o token
        DecodedJWT decodedJWT = decodedToken(token);
        UserDetails userDetails = this.userDetailsService
                .loadUserByUsername(decodedJWT.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodedToken(String token) {
        //Este método decoded precisa ler esse token e dividir em objetos que eu possa manipular através da minha aplicação
        Algorithm alg = Algorithm.HMAC256(secretKey.getBytes());
        //Se eu não passar o algoritmo correto ele n vai conseguir abrir o token e n vai conseguir validar ele
        JWTVerifier verifier = JWT.require(alg).build();
        //Agora vem efetivamente a parte de decodificar o token
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT;
    }

    //Agora precisamos validar o Token quando a pessoa tiver autenticando na aplicação, precisamos verificar se o token é valido ou não
    public String resolveToken(HttpServletRequest req) {
        //Vamos pegar os cabeçalhos da requisição. Esse cara sempre vai ser enviado na requsição com um token
        String bearerToken = req.getHeader("Authorization");

        //Bearer ye=chhjvkhfriuhcdjksbfjhbvhjfbjhvf
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            //Aqui estou deletando o trecho de código ontem o tem o "Bearer "
            return bearerToken.substring("Bearer ".length());
        }
        return null;
    }

    //Agora teremos um método para validar o Token
    public boolean validateToken(String token) {
        DecodedJWT decodedJWT = decodedToken(token);
        try {
            //Verificando se ele já expirou
            if (decodedJWT.getExpiresAt().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token!");
        }
    }
}




