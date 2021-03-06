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

package fi.helsinki.opintoni.integration.oodi.courseunitrealisation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OodiCourseUnitRealisationTeacher {

    @JsonProperty("full_name")
    public String fullName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("calling_name")
    public String callingName;

    @JsonProperty("first_names")
    public String firstNames;

    @JsonProperty("email")
    public String email;

    @JsonProperty("teacher_id")
    public String teacherId;

    @JsonProperty("teacher_role_code")
    public Integer teacherRoleCode;

}