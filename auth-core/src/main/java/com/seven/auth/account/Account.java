package com.seven.auth.account;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name="account")
@Data
@ToString
@NoArgsConstructor

public class Account implements Serializable, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false,length=15)
    private String phoneNo;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate dob;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime dateCreated;

    @UpdateTimestamp
    @Column
    private ZonedDateTime dateUpdated;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public Account(String firstName, String lastName, String phoneNo, String email, String password, LocalDate dob, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNo = phoneNo;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority sga = new SimpleGrantedAuthority("ROLE_"+this.role.name().toUpperCase());
        Set<SimpleGrantedAuthority> sgaSet = Arrays.stream(role.privileges).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        sgaSet.add(sga);
        return sgaSet;
    }
    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword(){return this.password;}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
