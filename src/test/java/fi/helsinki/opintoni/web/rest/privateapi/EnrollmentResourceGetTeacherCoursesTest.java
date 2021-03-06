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

package fi.helsinki.opintoni.web.rest.privateapi;

import fi.helsinki.opintoni.SpringTest;
import fi.helsinki.opintoni.web.WebConstants;
import org.junit.Test;
import org.springframework.http.MediaType;

import static fi.helsinki.opintoni.security.SecurityRequestPostProcessors.securityContext;
import static fi.helsinki.opintoni.security.TestSecurityContext.teacherSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnrollmentResourceGetTeacherCoursesTest extends SpringTest {

    private static final String PARENT_REALISATION_ID = "99903630";
    private static final String ROOT_REALISATION_ID = "99903629";
    private static final String STUDY_GROUP_WITHOUT_PARENT_TITLE = "Realisat... Study subgroup name";
    private static final String OFFICIAL_ROLE_NAME = "official";

    @Test
    public void thatTeacherCoursesAreReturned() throws Exception {
        expectTeacherCourses();

        mockMvc.perform(get("/api/private/v1/teachers/enrollments/courses")
            .with(securityContext(teacherSecurityContext()))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].code").value("10440"))
            .andExpect(jsonPath("$[0].name").value("Formulointi III"))
            .andExpect(jsonPath("$[0].startDate[0]").value(2015))
            .andExpect(jsonPath("$[0].startDate[1]").value(10))
            .andExpect(jsonPath("$[0].startDate[2]").value(26))
            .andExpect(jsonPath("$[0].startDate[3]").value(0))
            .andExpect(jsonPath("$[0].startDate[4]").value(0))
            .andExpect(jsonPath("$[0].endDate[0]").value(2016))
            .andExpect(jsonPath("$[0].endDate[1]").value(4))
            .andExpect(jsonPath("$[0].endDate[2]").value(27))
            .andExpect(jsonPath("$[0].endDate[3]").value(0))
            .andExpect(jsonPath("$[0].endDate[4]").value(0))
            .andExpect(jsonPath("$[0].webOodiUri").value("https://weboodi.helsinki.fi/"))
            .andExpect(jsonPath("$[0].isExam").value(false))
            .andExpect(jsonPath("$[0].isCancelled").value(false))
            .andExpect(jsonPath("$[0].realisationId").value(ROOT_REALISATION_ID))
            .andExpect(jsonPath("$[0].parentId").isEmpty())
            .andExpect(jsonPath("$[0].teacherRole").value(OFFICIAL_ROLE_NAME))
            .andExpect(jsonPath("$[1].code").value("10442"))
            .andExpect(jsonPath("$[1].isExam").value(true))
            .andExpect(jsonPath("$[1].isCancelled").value(true))
            .andExpect(jsonPath("$[1].parentId").value(PARENT_REALISATION_ID))
            .andExpect(jsonPath("$[2].name").value(STUDY_GROUP_WITHOUT_PARENT_TITLE));
    }

    private void expectTeacherCourses() {
        defaultTeacherRequestChain()
            .defaultCoursesWithImplementationsAndRealisations();
    }

}
