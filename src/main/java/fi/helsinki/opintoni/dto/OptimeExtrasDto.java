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

package fi.helsinki.opintoni.dto;

import com.google.common.collect.Lists;

import java.util.stream.Collectors;

public class OptimeExtrasDto {

    public String otherNotes;

    public String roomNotes;

    public String staffNotes;

    public OptimeExtrasDto(String otherNotes, String roomNotes, String staffNotes) {
        this.otherNotes = otherNotes;
        this.roomNotes = roomNotes;
        this.staffNotes = staffNotes;
    }

    @Override
    public String toString() {
        return Lists.newArrayList(roomNotes, staffNotes, otherNotes)
            .stream()
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(", "));
    }
}
