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
import org.junit.Test;

import static fi.helsinki.opintoni.security.SecurityRequestPostProcessors.securityContext;
import static fi.helsinki.opintoni.security.TestSecurityContext.studentSecurityContext;
import static fi.helsinki.opintoni.security.TestSecurityContext.teacherSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnrollmentResourcePermissionsTest extends SpringTest {

    @Test
    public void thatTeacherCannotGetStudentEvents() throws Exception {
        mockMvc.perform(get("/api/private/v1/students/enrollments/events")
            .with(securityContext(teacherSecurityContext())))
            .andExpect(status().isForbidden());
    }

    @Test
    public void thatTeacherCannotGetStudentCourses() throws Exception {
        mockMvc.perform(get("/api/private/v1/students/enrollments/courses")
            .with(securityContext(teacherSecurityContext())))
            .andExpect(status().isForbidden());
    }

    @Test
    public void thatStudentCannotGetTeacherEvents() throws Exception {
        mockMvc.perform(get("/api/private/v1/teachers/enrollments/events")
            .with(securityContext(studentSecurityContext())))
            .andExpect(status().isForbidden());
    }

    @Test
    public void thatStudentCannotGetTeacherCourses() throws Exception {
        mockMvc.perform(get("/api/private/v1/teachers/enrollments/courses")
            .with(securityContext(studentSecurityContext())))
            .andExpect(status().isForbidden());
    }

}
