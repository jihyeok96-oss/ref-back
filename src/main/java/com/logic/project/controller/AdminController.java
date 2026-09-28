package com.logic.project.controller;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    /*
     * 현재 프로젝트의 로그인 세션은
     *
     * session.setAttribute("loginMember", member);
     *
     * 방식으로 저장된다.
     *
     * 따라서 관리자 여부도 동일한 세션 객체를 기준으로 확인한다.
     */
    private boolean isAdmin(HttpSession session) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        return loginMember != null
                && loginMember.getRole() == MemberRole.ADMIN;
    }

    /*
     * 관리자 메인
     *
     * GET /admin/
     */
    @GetMapping("/")
    public String adminIndex(
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        List<Member> recentMembers =
                memberService.findRecentMembers();

        model.addAttribute(
                "recentMembers",
                recentMembers
        );

        return "admin/index";
    }

    /*
     * 회원 목록
     *
     * GET /admin/members
     */
    @GetMapping("/members")
    public String list(
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute(
                "members",
                memberService.findAll()
        );

        return "admin/member/list";
    }

    /*
     * 회원 상세
     *
     * GET /admin/members/{id}
     */
    @GetMapping("/members/{id}")
    public String detail(
            @PathVariable("id") Long id,
            HttpSession session,
            Model model
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute(
                "member",
                memberService.findById(id)
        );

        return "admin/member/detail";
    }

    /*
     * 회원 활성화
     *
     * POST /admin/members/{id}/activate
     */
    @PostMapping("/members/{id}/activate")
    public String activate(
            @PathVariable("id") Long id,
            HttpSession session
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        memberService.activate(id);

        return "redirect:/admin/members/" + id;
    }

    /*
     * 회원 비활성화
     *
     * POST /admin/members/{id}/deactivate
     */
    @PostMapping("/members/{id}/deactivate")
    public String deactivate(
            @PathVariable("id") Long id,
            HttpSession session
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        memberService.deactivate(id);

        return "redirect:/admin/members/" + id;
    }

    /*
     * 회원 강퇴
     *
     * POST /admin/members/{id}/kick
     */
    @PostMapping("/members/{id}/kick")
    public String kick(
            @PathVariable("id") Long id,
            HttpSession session
    ) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Member target =
                memberService.findById(id);

        /*
         * 관리자 계정은 강퇴할 수 없다.
         */
        if (target.getRole() == MemberRole.ADMIN) {
            return "redirect:/admin/members/"
                    + id
                    + "?error=admin";
        }

        memberService.kickoff(id);

        return "redirect:/admin/members/" + id;
    }
}