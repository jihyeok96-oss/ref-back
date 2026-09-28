package com.logic.project.api;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:80",
                "http://127.0.0.1:80"
        },
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class MemberRestController {

    private final MemberService memberService;


    // =========================================================
    // 현재 로그인 회원 조회
    // =========================================================

    /**
     * 현재 로그인한 회원 정보를 조회한다.
     *
     * GET /api/member/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(
            HttpSession session
    ) {

        Member loginMember =
                requireLogin(session);

        return ResponseEntity.ok(
                loginMember
        );
    }


    // =========================================================
    // 로그인
    // =========================================================

    /**
     * 로그인
     *
     * POST /api/member/login
     *
     * Request Body
     * {
     *     "loginId": "admin",
     *     "password": "1004"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        String loginId =
                body.get("loginId");

        String password =
                body.get("password");

        try {

            Member member =
                    memberService.login(
                            loginId,
                            password
                    );

            /*
             * 기존 MemberController와
             * 동일한 세션 이름을 사용한다.
             */
            session.setAttribute(
                    "loginMember",
                    member
            );

            return ResponseEntity.ok(
                    member
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 로그아웃
    // =========================================================

    /**
     * 로그아웃
     *
     * POST /api/member/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpSession session
    ) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "로그아웃되었습니다."
                )
        );
    }


    // =========================================================
    // 회원가입
    // =========================================================

    /**
     * 회원가입
     *
     * POST /api/member/signup
     *
     * Request Body 예시
     *
     * {
     *     "loginId": "user01",
     *     "password": "1234",
     *     "name": "홍길동",
     *     "email": "user01@test.com",
     *     "termsAgreed": "true",
     *     "privacyAgreed": "true"
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody Member member
    ) {

        try {

            memberService.signup(member);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "회원가입이 완료되었습니다."
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 마이페이지
    // =========================================================

    /**
     * 마이페이지 회원정보 조회
     *
     * GET /api/member/mypage
     */
    @GetMapping("/mypage")
    public ResponseEntity<?> mypage(
            HttpSession session
    ) {

        Member loginMember =
                requireLogin(session);

        /*
         * DB에서 최신 회원정보를 조회한다.
         */
        Member member =
                memberService.findById(
                        loginMember.getId()
                );

        /*
         * 세션도 최신 회원정보로 갱신한다.
         */
        session.setAttribute(
                "loginMember",
                member
        );

        return ResponseEntity.ok(
                member
        );
    }


    // =========================================================
    // 회원정보 수정
    // =========================================================

    /**
     * 회원정보 수정
     *
     * PUT /api/member/me
     *
     * Request Body
     *
     * {
     *     "name": "홍길동",
     *     "email": "test@test.com",
     *     "password": "1234"
     * }
     *
     * password는 변경하지 않을 경우
     * 빈 문자열 또는 null을 사용할 수 있다.
     */
    @PutMapping("/me")
    public ResponseEntity<?> update(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLogin(session);

        String name =
                body.get("name");

        String email =
                body.get("email");

        String password =
                body.get("password");

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
             * 세션의 회원정보도 갱신
             */
            session.setAttribute(
                    "loginMember",
                    updated
            );

            return ResponseEntity.ok(
                    updated
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 회원탈퇴
    // =========================================================

    /**
     * 회원탈퇴
     *
     * DELETE /api/member/me
     *
     * Request Body
     *
     * {
     *     "password": "1234"
     * }
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> delete(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLogin(session);

        String password =
                body.get("password");

        try {

            /*
             * 회원탈퇴
             */
            memberService.delete(
                    loginMember.getId(),
                    password
            );

            /*
             * 탈퇴 완료 후
             * 세션 전체 제거
             */
            session.invalidate();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "회원탈퇴가 완료되었습니다."
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // 로그인 여부 확인
    // =========================================================

    /**
     * 로그인 여부 확인
     *
     * GET /api/member/check
     *
     * 로그인 상태
     * {
     *     "login": true,
     *     "member": {...}
     * }
     *
     * 로그아웃 상태
     * {
     *     "login": false
     * }
     */
    @GetMapping("/check")
    public ResponseEntity<?> check(
            HttpSession session
    ) {

        Member loginMember =
                (Member) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null) {

            return ResponseEntity.ok(
                    Map.of(
                            "login",
                            false
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "login",
                        true,
                        "member",
                        loginMember
                )
        );
    }


    // =========================================================
    // 관리자 여부 확인
    // =========================================================

    /**
     * 관리자 여부 확인
     *
     * GET /api/member/admin-check
     */
    @GetMapping("/admin-check")
    public ResponseEntity<?> adminCheck(
            HttpSession session
    ) {

        Member loginMember =
                requireLogin(session);

        boolean isAdmin =
                loginMember.getRole()
                        == MemberRole.ADMIN;

        return ResponseEntity.ok(
                Map.of(
                        "admin",
                        isAdmin
                )
        );
    }


    // =========================================================
    // 로그인 필수 처리
    // =========================================================

    /**
     * 로그인 회원을 조회한다.
     *
     * 기존 프로젝트의 로그인 방식과 동일하게
     * "loginMember" 세션을 사용한다.
     */
    private Member requireLogin(
            HttpSession session
    ) {

        Member loginMember =
                (Member) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null) {

            throw new IllegalStateException(
                    "로그인이 필요합니다."
            );
        }

        return loginMember;
    }
}
