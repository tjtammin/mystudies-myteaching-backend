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
import fi.helsinki.opintoni.localization.Language;
import org.junit.Test;
import org.springframework.http.MediaType;
import java.util.Locale;

import static fi.helsinki.opintoni.security.SecurityRequestPostProcessors.securityContext;
import static fi.helsinki.opintoni.security.TestSecurityContext.studentSecurityContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseResourceTest extends SpringTest {

    private final static String LEARNING_OPPORTUNITY_ID_FIRST = "405437";
    private final static String LEARNING_OPPORTUNITY_ID_SECOND = "405438";
    private final static String LEARNING_OPPORTUNITY_IDS = "405437,405438";

    private final static String EXPECTED_FIRST_COURSE_NAME_FI = "VIE-alueen historian tulkinnat (XAK291V)";
    private final static String EXPECTED_FIRST_COURSE_NAME_EN = "Historical Interpretations of Russia and Eastern Europe (XAK291V)";
    private final static String EXPECTED_FIRST_COURSE_NAME_SV = "Historiska tolkningar om Ryssland och Östeuropa (XAK291V)";

    private final static String EXPECTED_SECOND_COURSE_NAME_FI = "VIE-alueen historian tulkinnat (B)";;
    private final static String EXPECTED_SECOND_COURSE_NAME_EN = "Historical Interpretations of Russia and Eastern Europe (B)";
    private final static String EXPECTED_SECOND_COURSE_NAME_SV = "Historiska tolkningar om Ryssland och Östeuropa (B)";

    private void testCourseNamesWithLocale(String languageCode, String expectedFirstName, String expectedSecondName) throws Exception{
        defaultOodiCourseNamesRequestChain().courseName(LEARNING_OPPORTUNITY_ID_FIRST, "learningopportunity_a.json");
        defaultOodiCourseNamesRequestChain().courseName(LEARNING_OPPORTUNITY_ID_SECOND, "learningopportunity_b.json");

        mockMvc.perform(get("/api/private/v1/courses/names?learningOpportunities=" + LEARNING_OPPORTUNITY_IDS)
            .with(securityContext(studentSecurityContext()))
            .locale(new Locale(languageCode))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].learningOpportunityName").value(expectedFirstName))
            .andExpect(jsonPath("$[1].learningOpportunityName").value(expectedSecondName));
    }

    @Test
    public void thatCourseNamesAreReturnedInFinnish() throws Exception {
        testCourseNamesWithLocale(
            Language.FI.getCode(),
            EXPECTED_FIRST_COURSE_NAME_FI,
            EXPECTED_SECOND_COURSE_NAME_FI);
    }

    @Test
    public void thatCourseNamesAreReturnedInEnglish() throws Exception {
        testCourseNamesWithLocale(
            Language.EN.getCode(),
            EXPECTED_FIRST_COURSE_NAME_EN,
            EXPECTED_SECOND_COURSE_NAME_EN);
    }

    @Test
    public void thatCourseNamesAreReturnedInSwedish() throws Exception {
        testCourseNamesWithLocale(
            Language.SV.getCode(),
            EXPECTED_FIRST_COURSE_NAME_SV,
            EXPECTED_SECOND_COURSE_NAME_SV);
    }

}
