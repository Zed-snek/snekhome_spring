package ed.back_snekhome.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.security.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;



@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "nickname"} ) )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    private Long idAccount;

    private String password;
    private String name;
    private String surname;
    private String email;
    private LocalDate registration;

    private String nickname;
    private String nicknameColor;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled; //used in UserDetails service

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL) //LAZY = loads only when is required. EAGER = loads all elements
    private List<UserImage> images;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InfoTag> tags;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    //Next two methods are not required due to @Getter annotation above, they are left here to understand they are Overrided from UserDetails
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }

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



}
