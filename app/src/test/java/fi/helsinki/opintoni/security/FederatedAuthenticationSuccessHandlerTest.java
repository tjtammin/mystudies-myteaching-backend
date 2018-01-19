/*
 * This file is part of MystudiesMyteaching application.
 *
 * MystudiesMyteaching application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MystudiesMyteaching application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MystudiesMyteaching application.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.helsinki.opintoni.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.helsinki.opintoni.domain.User;
import fi.helsinki.opintoni.security.enumerated.SAMLEduPersonAffiliation;
import fi.helsinki.opintoni.service.SessionService;
import fi.helsinki.opintoni.service.TimeService;
import fi.helsinki.opintoni.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fi.helsinki.opintoni.config.Constants.NG_TRANSLATE_LANG_KEY;
import static fi.helsinki.opintoni.config.Constants.OPINTONI_HAS_LOGGED_IN;
import static fi.helsinki.opintoni.localization.Language.*;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FederatedAuthenticationSuccessHandlerTest {
    private static final List<String> availableLanguages = newArrayList(FI.getCode(), EN.getCode(), SV.getCode());
    private static final String UNSUPPORTED_LANGUAGE = "en_US";

    private static final String EDU_PRINCIPAL_NAME = "eduPrincipalName";
    private static final String OODI_PERSON_ID = "oodiPersonId";

    private final Authentication authentication = mock(Authentication.class);

    @Mock
    private ObjectMapper mapper;

    @Mock
    private UserService userService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TimeService timeService;

    @Mock
    private Environment env;

    @InjectMocks
    private FederatedAuthenticationSuccessHandler handler;

    private void setupMocks(String appUserPreferredLanguage) {
        AppUser appUser = new AppUser.AppUserBuilder()
            .eduPersonPrincipalName(EDU_PRINCIPAL_NAME)
            .studentNumber("1234")
            .oodiPersonId(OODI_PERSON_ID)
            .preferredLanguage(appUserPreferredLanguage)
            .eduPersonAffiliations(singletonList(SAMLEduPersonAffiliation.STUDENT))
            .build();

        when(authentication.getPrincipal()).thenReturn(appUser);
        when(env.getRequiredProperty("locale.available", List.class)).thenReturn(availableLanguages);
    }

    @Test
    public void thatOldUserDoesNotResultInNewUser() throws Exception {
        setupMocks(FI.getCode());
        Optional<User> user = Optional.of(new User());
        HttpServletResponse response = mockResponse();

        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(user);

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authentication);

        verify(userService, never()).createNewUser(any(AppUser.class));
    }

    @Test
    public void thatMissingOodiPersonIdIsUpdated() throws Exception {
        setupMocks(FI.getCode());
        Optional<User> user = Optional.of(new User());
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(user);

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), mockResponse(), authentication);

        verify(userService, times(1)).save(argThat(new UserMatcher()));
    }

    @Test
    public void thatNewUserIsSaved() throws Exception {
        setupMocks(FI.getCode());
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), mockResponse(), authentication);

        verify(userService, times(1)).createNewUser(any(AppUser.class));
    }

    @Test
    public void thatLanguageCookieIsAddedForUserPreferredLanguageInFirstLogin() throws Exception {
        String langCode = FI.getCode();

        setupMocks(langCode);
        HttpServletResponse response = mockResponse();
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authentication);

        verify(response, times(1)).addCookie(argThat(new LangCookieMatcher(langCode)));
    }

    @Test
    public void thatLanguageCookieIsNotAddedForUserInLaterLogins() throws Exception {
        String langCode = FI.getCode();

        setupMocks(langCode);
        HttpServletResponse response = mockResponse();
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(Optional.empty());

        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie(OPINTONI_HAS_LOGGED_IN, "true")};
        when(request.getCookies()).thenReturn(cookies);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response, times(0)).addCookie(argThat(new LangCookieMatcher(langCode)));
    }

    @Test
    public void languageCookieIsNotAddedIfUserPreferredLanguageIsNotSupported() throws Exception {
        setupMocks(UNSUPPORTED_LANGUAGE);
        HttpServletResponse response = mockResponse();
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authentication);

        verify(response, times(0)).addCookie(argThat(new LangCookieMatcher(UNSUPPORTED_LANGUAGE)));
    }

    @Test
    public void thatHasLoggedInCookieIsAdded() throws IOException, ServletException {
        setupMocks(FI.getCode());
        HttpServletResponse response = mockResponse();
        when(userService.findFirstByEduPersonPrincipalName(EDU_PRINCIPAL_NAME)).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authentication);

        verify(response, times(1)).addCookie(argThat(new HasLoggedInCookieMatcher()));
    }

    private HttpServletResponse mockResponse() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        return response;
    }

    private static class LangCookieMatcher implements ArgumentMatcher<Cookie> {

        private final String langCode;

        public LangCookieMatcher(String langCode) {
            this.langCode = langCode;
        }

        @Override
        public boolean matches(Cookie cookie) {
            return NG_TRANSLATE_LANG_KEY.equals(cookie.getName()) && langCode.equals(cookie.getValue());
        }
    }

    private static class HasLoggedInCookieMatcher implements ArgumentMatcher<Cookie> {

        @Override
        public boolean matches(Cookie cookie) {
            return OPINTONI_HAS_LOGGED_IN.equals(cookie.getName()) && "true".equals(cookie.getValue());
        }
    }

    private static class UserMatcher implements ArgumentMatcher<User> {

        @Override
        public boolean matches(User user) {
            return user.oodiPersonId.equals(OODI_PERSON_ID);
        }
    }
}
