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

package fi.helsinki.opintoni.web.requestchain;

import fi.helsinki.opintoni.server.CoursePageServer;
import fi.helsinki.opintoni.server.OodiServer;

import java.util.Arrays;

import static fi.helsinki.opintoni.web.TestConstants.*;

public class TeacherRequestChain {

    private static final String TEACHER_COURSE_IMPLEMENTATION_FILE = "teacher_course.json";

    private final OodiServer oodiServer;
    private final CoursePageServer coursePageServer;
    private final String teacherNumber;
    private final String sinceDateString;

    public TeacherRequestChain(String teacherNumber, String sinceDateString, OodiServer oodiServer, CoursePageServer coursePageServer) {
        this.oodiServer = oodiServer;
        this.coursePageServer = coursePageServer;
        this.teacherNumber = teacherNumber;
        this.sinceDateString = sinceDateString;
    }

    public TeacherRequestChain courses() {
        oodiServer.expectTeacherCoursesRequest(teacherNumber, sinceDateString);
        return this;
    }

    public TeacherRequestChain defaultCoursesWithImplementationsAndRealisations() {
        return courses()
            .defaultCourseImplementation()
            .and()
            .examCourseImplementation()
            .and()
            .courseImplementationWithRealisationId(POSITION_STUDYGROUP_TEACHER_COURSE_REALISATION_ID)
            .and()
            .courseUnitRealisation(TEACHER_COURSE_REALISATION_ID)
            .cancelledCourseUnitRealisation(EXAM_TEACHER_COURSE_REALISATION_ID)
            .positionStudygroupsetCourseUnitRealisation(POSITION_STUDYGROUPSET_TEACHER_COURSE_REALISATION_ID)
            .positionStudygroupCourseUnitRealisation(POSITION_STUDYGROUP_TEACHER_COURSE_REALISATION_ID);
    }

    public TeacherRequestChain courses(String responseFile) {
        oodiServer.expectTeacherCoursesRequest(teacherNumber, sinceDateString, responseFile);
        return this;
    }

    public TeacherRequestChain events() {
        oodiServer.expectTeacherEventsRequest(teacherNumber);
        return this;
    }

    public CourseImplementationRequestChain<TeacherRequestChain> defaultCourseImplementation() {
        return courseImplementationWithRealisationId(TEACHER_COURSE_REALISATION_ID);
    }

    public CourseImplementationRequestChain<TeacherRequestChain> examCourseImplementation() {
        return courseImplementationWithRealisationId(EXAM_TEACHER_COURSE_REALISATION_ID);
    }

    public CourseImplementationRequestChain<TeacherRequestChain> courseImplementationWithRealisationId(String realisationId) {
        return courseImplementation(realisationId, TEACHER_COURSE_IMPLEMENTATION_FILE);
    }

    public TeacherRequestChain courseUnitRealisation(String realisationId) {
        oodiServer.expectCourseUnitRealisationRequest(realisationId);
        return this;
    }

    public TeacherRequestChain cancelledCourseUnitRealisation(String realisationId) {
        oodiServer.expectCancelledCourseUnitRealisationRequest(realisationId);
        return this;
    }

    public TeacherRequestChain positionStudygroupsetCourseUnitRealisation(String realisationId) {
        oodiServer.expectPositionStudygroupsetCourseUnitRealisationRequest(realisationId);
        return this;
    }

    public TeacherRequestChain positionStudygroupCourseUnitRealisation(String realisationId) {
        oodiServer.expectPositionStudygroupCourseUnitRealisationRequest(realisationId);
        return this;
    }

    public CourseImplementationRequestChain<TeacherRequestChain> courseImplementation(
        String courseImplementationId, String responseFile) {
        CourseImplementationRequestChain<TeacherRequestChain> builder =
            new CourseImplementationRequestChain<>(
                this,
                coursePageServer,
                courseImplementationId);
        return builder.expectImplementation(responseFile);
    }
}
