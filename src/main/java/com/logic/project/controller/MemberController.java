package com.logic.project.controller;

import com.logic.project.domain.Member;
import com.logic.project.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;


    // =========================================================
    // 로그인 화면
    // =========================================================

    /**
     * 로그인 화면 로딩
     */
    @GetMapping("/login")
    public String loginForm() {

        return "member/login";
    }


    // =========================================================
    // 로그인
    // =========================================================

    /**
     * 로그인 처리
     *
     * 로그인 성공 시
     * HttpSession에 "loginMember"라는 이름으로
     * 로그인한 회원 정보를 저장한다.
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password,
            HttpSession session,
            Model model
    ) {

        try {

            // 회원 로그인 검증
            Member member =
                    memberService.login(loginId, password);

            /*
             * 로그인 성공
             *
             * 기존 프로젝트에서 사용하던
             * "loginMember" 세션 이름을 그대로 사용한다.
             */
            session.setAttribute(
                    "loginMember",
                    member
            );

            return "redirect:/";

        } catch (IllegalArgumentException e) {

            // 로그인 실패
            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "member/login";
        }
    }


    // =========================================================
    // 로그아웃
    // =========================================================

    /**
     * 로그아웃 처리
     *
     * 현재 세션을 완전히 제거한다.
     */
    @GetMapping("/logout")
    public String logout(
            HttpSession session
    ) {

        session.invalidate();

        return "redirect:/";
    }


    // =========================================================
    // 회원가입
    // =========================================================

    /**
     * 약관 화면 로딩
     */
    @GetMapping("/terms")
    public String terms() {

        return "member/terms";
    }


    /**
     * 회원가입 화면 로딩
     */
    @GetMapping("/signup")
    public String signupForm() {

        return "member/signup";
    }


    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(
            @ModelAttribute Member member,
            Model model
    ) {

        try {

            memberService.signup(member);

            return "redirect:/member/login?signupSuccess=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "member/signup";
        }
    }


    // =========================================================
    // 마이페이지
    // =========================================================

    /**
     * 마이페이지
     */
    @GetMapping("/mypage")
    public String mypageLoad(
            HttpSession session,
            Model model
    ) {

        /*
         * 세션에서 로그인 회원 조회
         */
        Member loginMember =
                (Member) session.getAttribute("loginMember");

        /*
         * 로그인하지 않은 경우
         */
        if (loginMember == null) {

            return "redirect:/member/login";
        }

        /*
         * DB에서 최신 회원 정보를 다시 조회한다.
         */
        Member member =
                memberService.findById(
                        loginMember.getId()
                );

        /*
         * 최신 회원 정보로 세션 갱신
         */
        session.setAttribute(
                "loginMember",
                member
        );

        /*
         * View로 회원 정보 전달
         */
        model.addAttribute(
                "member",
                member
        );

        return "member/mypage";
    }


    // =========================================================
    // 회원정보 수정
    // =========================================================

    /**
     * 회원정보 수정 화면
     */
    @GetMapping("/edit")
    public String editForm(
            HttpSession session,
            Model model
    ) {

        /*
         * 로그인 회원 조회
         */
        Member loginMember =
                (Member) session.getAttribute("loginMember");

        /*
         * 로그인하지 않은 경우
         */
        if (loginMember == null) {

            return "redirect:/member/login";
        }

        /*
         * DB에서 최신 정보 조회
         */
        Member member =
                memberService.findById(
                        loginMember.getId()
                );

        model.addAttribute(
                "member",
                member
        );

        return "member/edit";
    }


    /**
     * 회원정보 수정 처리
     */
    @PostMapping("/edit")
    public String edit(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(
                    name = "password",
                    required = false
            ) String password,
            HttpSession session,
            Model model
    ) {

        /*
         * 현재 로그인 회원 조회
         */
        Member loginMember =
                (Member) session.getAttribute("loginMember");

        /*
         * 로그인하지 않은 경우
         */
        if (loginMember == null) {

            return "redirect:/member/login";
        }

        try {

            /*
             * 회원정보 수정
             */
            memberService.update(
                    loginMember.getId(),
                    name,
                    email,
                    password
            );

            /*
             * 수정된 최신 회원정보 조회
             */
            Member updated =
                    memberService.findById(
                            loginMember.getId()
                    );

            /*
             * 세션의 회원정보도 최신 정보로 갱신
             */
            session.setAttribute(
                    "loginMember",
                    updated
            );

            return "redirect:/member/mypage";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            /*
             * 수정 화면에 최신 회원정보 전달
             */
            Member member =
                    memberService.findById(
                            loginMember.getId()
                    );

            model.addAttribute(
                    "member",
                    member
            );

            return "member/edit";
        }
    }


    // =========================================================
    // 회원탈퇴
    // =========================================================

    /**
     * 회원탈퇴 화면
     */
    @GetMapping("/delete")
    public String deleteForm(
            HttpSession session
    ) {

        /*
         * 로그인 여부 확인
         */
        if (session.getAttribute("loginMember") == null) {

            return "redirect:/member/login";
        }

        return "member/delete";
    }


    /**
     * 회원탈퇴 처리
     */
    @PostMapping("/delete")
    public String delete(
            @RequestParam("password") String password,
            HttpSession session,
            Model model
    ) {

        /*
         * 현재 로그인 회원 조회
         */
        Member loginMember =
                (Member) session.getAttribute("loginMember");

        /*
         * 로그인하지 않은 경우
         */
        if (loginMember == null) {

            return "redirect:/member/login";
        }

        try {

            /*
             * 회원탈퇴
             */
            memberService.delete(
                    loginMember.getId(),
                    password
            );

            /*
             * 회원탈퇴가 완료되었으므로
             * 로그인 세션 전체 제거
             */
            session.invalidate();

            return "redirect:/?deleteSuccess=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "member/delete";
        }
    }
}