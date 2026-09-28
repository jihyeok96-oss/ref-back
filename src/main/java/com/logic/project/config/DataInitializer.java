package com.logic.project.config;
import com.logic.project.domain.*; 
import com.logic.project.repository.MemberRepository; 
import lombok.RequiredArgsConstructor; 
import org.springframework.boot.CommandLineRunner; 
import org.springframework.context.annotation.*; 
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration 
@RequiredArgsConstructor 
public class DataInitializer {

	private final MemberRepository repo; 
	private final PasswordEncoder encoder;

	@Bean
    CommandLineRunner initAdmin(){
		return args -> {
			if(repo.findByLoginId("admin").isEmpty()) repo.save(Member.builder().loginId("admin").password(encoder.encode("1234")).name("관리자").email("admin@logic.local").role(MemberRole.ADMIN).status(MemberStatus.ACTIVE).termsAgreed(true).privacyAgreed(true).build());
		};
	} 
}
