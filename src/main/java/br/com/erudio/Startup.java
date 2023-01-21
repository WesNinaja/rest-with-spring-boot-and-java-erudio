package br.com.erudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
//
//import java.util.HashMap;
//import java.util.Map;

@SpringBootApplication
public class Startup {

	public static void main(String[] args) {
		SpringApplication.run(Startup.class, args);

		/*
		 *Como faremos para gerar uma senha?
		 * Com o trecho de código abaixo. Isso vai imprimir no console uma senha encriptada, no caso admin234
		 */
//		Pbkdf2PasswordEncoder pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder("", 8, 185000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
//
//
//		Map<String, PasswordEncoder> encoders = new HashMap<>();
//		encoders.put("pbkdf2", pbkdf2PasswordEncoder);
//		DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
//		passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2PasswordEncoder);
//
//		String result1 = passwordEncoder.encode("admin123");
//		String result2 = passwordEncoder.encode("admin234");
//		System.out.println("My hash result1" + result1);
//		System.out.println("My hash result2" + result2);
	}




}
