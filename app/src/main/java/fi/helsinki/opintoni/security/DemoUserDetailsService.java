package fi.helsinki.opintoni.security;

import com.google.common.collect.ImmutableMap;
import fi.helsinki.opintoni.config.Constants;
import fi.helsinki.opintoni.security.enumerated.SAMLEduPersonAffiliation;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

@Component("demoUserDetailsService")
@Profile({
    Constants.SPRING_PROFILE_DEMO
})
public class DemoUserDetailsService implements UserDetailsService {
    private final ImmutableMap<String, Supplier<UserDetails>> USER_CONF = ImmutableMap.of(
        "doo_8", this::newTeacher,
        "doo_9", this::newStudent,
        "doo_10", this::newHybrid,
        "doo_11", this::newOpenUniStudent
    );

    @Override
    public UserDetails loadUserByUsername(String username) {
        String normalizedUsername = username.toLowerCase();

        if(USER_CONF.containsKey(normalizedUsername)) {
            return USER_CONF.get(normalizedUsername).get();
        } else {
            throw new UsernameNotFoundException("Unknown user: " + normalizedUsername);
        }
    }

    private UserDetails newTeacher() {
        return new AppUser.AppUserBuilder()
            .eduPersonPrincipalName("doo_8@helsinki.fi")
            .eduPersonAffiliations(singletonList(SAMLEduPersonAffiliation.FACULTY))
            .eduPersonPrimaryAffiliation(SAMLEduPersonAffiliation.FACULTY)
            .email("doo_8@example.com")
            .commonName("Eero Jukola")
            .employeeNumber("007505")
            .oodiPersonId("109155648")
            .role(AppUser.Role.ADMIN)
            .build();
    }

    private UserDetails newStudent() {
        return new AppUser.AppUserBuilder()
            .eduPersonPrincipalName("doo_9@helsinki.fi")
            .eduPersonAffiliations(Arrays.asList(SAMLEduPersonAffiliation.MEMBER, SAMLEduPersonAffiliation.STUDENT))
            .eduPersonPrimaryAffiliation(SAMLEduPersonAffiliation.STUDENT)
            .email("doo_9@example.com")
            .commonName("Venla Jukola")
            .studentNumber("010096350")
            .oodiPersonId("109155672")
            .build();
    }

    private UserDetails newHybrid() {
        return new AppUser.AppUserBuilder()
            .eduPersonPrincipalName("doo_10@helsinki.fi")
            .eduPersonAffiliations(singletonList(SAMLEduPersonAffiliation.STUDENT))
            .eduPersonPrimaryAffiliation(SAMLEduPersonAffiliation.FACULTY)
            .email("doo_10@example.com")
            .commonName("Juhani Jukola")
            .employeeNumber("007507")
            .studentNumber("010095623")
            .oodiPersonId("109155650")
            .build();
    }

    private UserDetails newOpenUniStudent() {
        return new AppUser.AppUserBuilder()
            .eduPersonPrincipalName("doo_11@helsinki.fi")
            .eduPersonAffiliations(Arrays.asList(SAMLEduPersonAffiliation.MEMBER, SAMLEduPersonAffiliation.STUDENT))
            .eduPersonPrimaryAffiliation(SAMLEduPersonAffiliation.STUDENT)
            .email("doo_11@example.com")
            .commonName("Kaisa Rajamäki")
            .studentNumber("010094899")
            .oodiPersonId("109155680")
            .build();
    }
}