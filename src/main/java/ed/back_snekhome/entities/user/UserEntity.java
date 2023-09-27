package ed.back_snekhome.entities.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.security.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "nickname"} ) )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAccount;

    private String password;

    @Max(value = 17, message = "Name should contain not more than 16 symbols")
    private String name;

    @Max(value = 22, message = "Surname should contain not more than 22 symbols")
    private String surname;

    @Email(message = "The provided value is not e-mail")
    private String email;

    private LocalDate registration;

    @NotBlank(message = "Nickname must not be blank")
    @Max(value = 19, message = "Nickname should contain not more than 18 symbols")
    private String nickname;

    private String nicknameColor;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled; //used in UserDetails service

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL) //LAZY = loads only when is required. EAGER = loads all elements
    private List<UserImage> images;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InfoTag> tags;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Membership> memberships;

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
