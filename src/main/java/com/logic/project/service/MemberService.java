package com.logic.project.service;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.domain.MemberStatus;
import com.logic.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원 가입
    public Member signup(Member member) {
        //이미 사용 중인 아이디인지 확인
        if(memberRepository.existsByLoginId(member.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        //이미 사용 중인 이메일인지 확인
        if(memberRepository.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        //이용약관에 동의하였는지 확인
        if(!member.isTermsAgreed()) {
            throw new IllegalArgumentException("이용약관에 동의하셔야 합니다.");
        }

        //개인정보 수집 및 이용에 동의하였는지 확인
        if(!member.isPrivacyAgreed()) {
            throw new IllegalArgumentException("개인정보 수집 및 이용에 동의해햐 합니다.");
        }

        //비밀번호 암호화
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        //ROLE 값의 기본값 지정
        member.setRole(MemberRole.USER);

        //상태(Status) 값의 기본값 지정
        member.setStatus(MemberStatus.ACTIVE);

        //가입하고, 가입정보를 리턴
        return memberRepository.save(member);
    }

    //로그인
    @Transactional(readOnly = true)
    public Member login(String loginId, String password) {

        //회원의 존재 여부 확인
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(
                () -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.")
        );

        //활성화되어 있는 회원인지 확인
        if(member.getStatus() != MemberStatus.ACTIVE) {
            if(member.getStatus() == MemberStatus.KICKED) {
                throw new IllegalArgumentException("강퇴된 회원입니다.");
            }
            throw new IllegalArgumentException("비활성화된 회원입니다.");
        }

        //비밀번호가 올바른지 확인
        //1234 => $a1^12Hj
        if(!passwordEncoder.matches(password, member.getPassword())){
           throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        //모두 일치하고 활성화되어 있는 회원이면, 회원 정보 리턴
        return member;
    }

    //id로 회원 조회
    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 회원이 존재하지 않습니다.")
        );
    }


    //loginId로 조회 - 마이페이지, 관리자 회원 상세 정보보기
    @Transactional(readOnly = true)
    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId).orElseThrow(
                () -> new IllegalArgumentException("해당 회원이 존재하지 않습니다.")
        );
    }


    //회원 목록 - 관리자
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAllByOrderByIdDesc();
    }

    //회원 정보 수정
    public void update(
            Long id, String name,
            String email, String password
    ) {
        Member member = findById(id); //검색

        if(!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        member.setName(name);
        member.setEmail(email);

        //만약, 비밀번호가 빈 칸이 아닌 경우에만 비밀번호 변경
        if(password != null && !password.isBlank()) {
            member.setPassword(passwordEncoder.encode(password));
        }
        //memberRepository.save(member);
    }

    //회원 탈퇴
    public void delete(Long id, String password) {
        Member member = findById(id);
        //비밀번호 맞는지 다시 확인
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("회원정보가 일치하지 않습니다.");
        }
        member.setStatus(MemberStatus.INACTIVE);    //비활성화
        //memberRepository.save(member);
    }

    //관리자 - 회원 활성화/비활성화/강퇴
    public void activate(Long id) {
        Member member = findById(id);
        //활성화
        member.setStatus(MemberStatus.ACTIVE);
        //memberRepository.save(member);
    }

    public void deactivate(Long id) {
        Member member = findById(id);
        //비활성화
        member.setStatus(MemberStatus.INACTIVE);
        //memberRepository.save(member);
    }

    public void kickoff(Long id) {
        Member member = findById(id);
        //회원 강퇴
        member.setStatus(MemberStatus.KICKED);
        //memberRepository.save(member);
    }


    //최근 가입 회원 5명 조회
    @Transactional(readOnly = true)
    public List<Member> findRecentMembers() {

        return memberRepository
                .findTop5ByOrderByCreatedAtDesc();
    }
}
