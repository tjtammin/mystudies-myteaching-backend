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

package fi.helsinki.opintoni.service;

import fi.helsinki.opintoni.domain.DegreeProgramme;
import fi.helsinki.opintoni.domain.OfficeHours;
import fi.helsinki.opintoni.domain.User;
import fi.helsinki.opintoni.dto.DegreeProgrammeDto;
import fi.helsinki.opintoni.dto.OfficeHoursDto;
import fi.helsinki.opintoni.dto.PublicOfficeHoursDto;
import fi.helsinki.opintoni.repository.DegreeProgrammeRepository;
import fi.helsinki.opintoni.repository.OfficeHoursRepository;
import fi.helsinki.opintoni.repository.UserRepository;
import fi.helsinki.opintoni.service.converter.OfficeHoursConverter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OfficeHoursService {

    // String used to catenate multiple office hours in getAll
    public static final String OFFICE_HOURS_JOIN = " ; ";

    private final OfficeHoursRepository officeHoursRepository;
    private final DegreeProgrammeRepository degreeProgrammeRepository;
    private final UserRepository userRepository;
    private final OfficeHoursConverter officeHoursConverter;


    @Autowired
    public OfficeHoursService(OfficeHoursRepository officeHoursRepository,
                              DegreeProgrammeRepository degreeProgrammeRepository,
                              UserRepository userRepository,
                              OfficeHoursConverter officeHoursConverter) {
        this.officeHoursRepository = officeHoursRepository;
        this.degreeProgrammeRepository = degreeProgrammeRepository;
        this.userRepository = userRepository;
        this.officeHoursConverter = officeHoursConverter;
    }

    private int compareNames(String name1,String name2) {
        String p1ToBeSorted = convertToSortableName(name1);
        String p2ToBeSorted = convertToSortableName(name2);
        return p1ToBeSorted.compareTo(p2ToBeSorted);
    }

    private static String convertToSortableName(String name){
        String[] parts = name.trim().split(" ");
        String sortableString = parts[parts.length-1];
        for (int i=0; i<parts.length-1;i++) {
            sortableString = sortableString + parts[i];
        }
        return sortableString;
    }

    public List<OfficeHoursDto> update(final Long userId, final List<OfficeHoursDto> officeHoursDtoList) {
        officeHoursRepository.deleteByUserId(userId);
        User user = userRepository.findOne(userId);
        return officeHoursDtoList.stream()
            .map(dto -> {
                OfficeHours officeHours = new OfficeHours();
                officeHours.user = user;
                officeHours.description = dto.description;
                officeHours.name = dto.name;
                officeHours.degreeProgrammes = degreeProgrammesFromDtos(dto.degreeProgrammes);
                return officeHoursRepository.save(officeHours);
            })
            .map(officeHoursConverter::toDto)
            .collect(Collectors.toList());
    }

    public void delete(final Long userId) {
        officeHoursRepository.deleteByUserId(userId);
    }

    public List<OfficeHoursDto> getByUserId(final Long userId) {
        List<OfficeHours> officeHours = officeHoursRepository.findByUserId(userId);
        return officeHours.stream()
            .map(officeHoursConverter::toDto)
            .collect(Collectors.toList());
    }

    public List<PublicOfficeHoursDto> getAll() {
        List<OfficeHours> allOfficeHours = officeHoursRepository.findAll();

        Map<String, List<OfficeHours>> groupedOfficeHours = allOfficeHours.stream()
            .collect(Collectors.groupingBy(oh -> oh.name));

        return groupedOfficeHours.keySet().stream()
            .sorted(this::compareNames)
            .map(name -> {
                PublicOfficeHoursDto officeHoursDto = new PublicOfficeHoursDto();

                officeHoursDto.degreeProgrammes = groupedOfficeHours.get(name).stream()
                    .flatMap(oh -> oh.degreeProgrammes.stream().map(dp -> dp.degreeCode))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

                officeHoursDto.officeHours = groupedOfficeHours.get(name).stream()
                    .map(oh -> oh.description)
                    .reduce("", this::joinHours);

                officeHoursDto.name = name;

                return officeHoursDto;
            }).collect(Collectors.toList());
    }

    private String joinHours(String sum, String next) {
        if (sum.trim().isEmpty()) {
            return next;
        }
        return sum + OFFICE_HOURS_JOIN + next;
    }

    private List<DegreeProgramme> degreeProgrammesFromDtos(List<DegreeProgrammeDto> degreeProgrammesDtos) {
        return degreeProgrammesDtos.stream()
            .map(dto -> dto.code)
            .distinct()
            .map(code -> {
                DegreeProgramme degreeProgramme = degreeProgrammeRepository
                    .findFirstByDegreeCode(code);
                if (degreeProgramme == null) {
                    degreeProgramme = new DegreeProgramme();
                    degreeProgramme.degreeCode = code;
                    degreeProgramme = degreeProgrammeRepository.save(degreeProgramme);
                }
                return degreeProgramme;
            })
            .collect(Collectors.toList());
    }

}
