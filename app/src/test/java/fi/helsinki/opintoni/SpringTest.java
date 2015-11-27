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

package fi.helsinki.opintoni;

import fi.helsinki.opintoni.config.AppConfiguration;
import fi.helsinki.opintoni.config.Constants;
import fi.helsinki.opintoni.security.AppUser;
import fi.helsinki.opintoni.server.*;
import fi.helsinki.opintoni.util.DateTimeUtil;
import fi.helsinki.opintoni.web.TestConstants;
import fi.helsinki.opintoni.web.requestchain.StudentRequestChain;
import fi.helsinki.opintoni.web.requestchain.TeacherRequestChain;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebAppConfiguration
@IntegrationTest
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
public abstract class SpringTest {

    private OodiServer oodiServer;
    private CoursePageServer coursePageServer;
    protected MetaDataServer metaDataServer;
    protected LeikiServer leikiServer;
    protected FlammaServer flammaServer;

    protected MockMvc mockMvc;

    @Autowired
    protected RestTemplate oodiRestTemplate;

    @Autowired
    protected RestTemplate coursePageRestTemplate;

    @Autowired
    protected RestTemplate leikiRestTemplate;

    @Autowired
    protected RestTemplate flammaRestTemplate;

    @Autowired
    protected RestTemplate metaDataRestTemplate;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SpringLiquibase springLiquibase;

    @Autowired
    protected AppConfiguration appConfiguration;

    @Before
    public void initRestServer() {
        oodiServer = new OodiServer(appConfiguration, oodiRestTemplate);
        coursePageServer = new CoursePageServer(appConfiguration, coursePageRestTemplate);
        leikiServer = new LeikiServer(appConfiguration, leikiRestTemplate);
        flammaServer = new FlammaServer(appConfiguration, flammaRestTemplate);
        metaDataServer = new MetaDataServer(metaDataRestTemplate);
        configureMockMvc();
    }

    @Before
    public void clearDatabase() throws LiquibaseException {
        springLiquibase.afterPropertiesSet();
    }

    @Before
    public void clearCaches() {
        cacheManager.getCacheNames()
            .stream()
            .map(cacheManager::getCache)
            .forEach(Cache::clear);
    }

    private void configureMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(springSecurityFilterChain)
            .build();
    }

    protected void configureStudentSecurityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(new TestingAuthenticationToken(
            new AppUser.AppUserBuilder()
                .studentNumber(TestConstants.STUDENT_NUMBER)
                .eduPersonPrincipalName("opiskelija@helsinki.fi")
                .oodiPersonId("1111")
                .build(),
            ""));
    }

    protected void configureTeacherSecurityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(new TestingAuthenticationToken(
            new AppUser.AppUserBuilder()
                .teacherNumber(TestConstants.TEACHER_NUMBER)
                .eduPersonPrincipalName("opettaja@helsinki.fi")
                .teacherFacultyCode("A10000")
                .oodiPersonId("2222")
                .build(),
            ""));
    }

    protected TeacherRequestChain defaultTeacherRequestChain() {
        return new TeacherRequestChain(TestConstants.TEACHER_NUMBER, DateTimeUtil.getSemesterStartDateString(LocalDate.now()), oodiServer, coursePageServer);
    }

    protected TeacherRequestChain teacherRequestChain(String teacherNumber) {
        return new TeacherRequestChain(teacherNumber, DateTimeUtil.getSemesterStartDateString(LocalDate.now()), oodiServer, coursePageServer);
    }

    protected StudentRequestChain defaultStudentRequestChain() {
        return new StudentRequestChain(TestConstants.STUDENT_NUMBER, oodiServer, coursePageServer);
    }

    protected StudentRequestChain studentRequestChain(String studentNumber) {
        return new StudentRequestChain(studentNumber, oodiServer, coursePageServer);
    }

}
