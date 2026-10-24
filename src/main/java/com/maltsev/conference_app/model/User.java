package com.maltsev.conference_app.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(length = 100)
    private String middleName;

    @Column(nullable = false, length = 64)
    private String emailLocal;

    @Column(nullable = false, length = 255)
    private String emailDomain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    public User(String lastName, String firstName, String middleName,
                String emailLocal, String emailDomain, Role role) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.emailLocal = emailLocal;
        this.emailDomain = emailDomain;
        this.role = role;
    }

    public String getFullName() {
        if (middleName == null || middleName.isBlank()) {
            return lastName + " " + firstName;
        }
        return lastName + " " + firstName + " " + middleName;
    }

    public String getEmail() {
        return emailLocal + "@" + emailDomain;
    }
}
