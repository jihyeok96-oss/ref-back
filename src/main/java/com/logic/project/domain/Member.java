package com.logic.project.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.USER;

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;

    private boolean termsAgreed;

    private boolean privacyAgreed;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    //private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if(role == null) {
            role = MemberRole.USER;
        }
        if(status == null) {
            status = MemberStatus.ACTIVE;
        }
        if(createdAt == null) {
            LocalDateTime.now();
        }
    }
}
