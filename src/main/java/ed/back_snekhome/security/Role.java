package ed.back_snekhome.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

public enum Role {
    USER, PARTNER, ADMIN;


    public Set<SimpleGrantedAuthority> getAuthorities(){
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }

    public static Set<SimpleGrantedAuthority> getAuthoritiesByName(String role){
        if(role.equals("ADMIN"))
            return ADMIN.getAuthorities();
        else if(role.equals("PARTNER"))
            return PARTNER.getAuthorities();
        else
            return USER.getAuthorities();
    }

}
