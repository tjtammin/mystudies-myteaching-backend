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

package fi.helsinki.opintoni.service.news;

import fi.helsinki.opintoni.dto.NewsDto;
import fi.helsinki.opintoni.security.AppUser;
import fi.helsinki.opintoni.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
public class NewsService {

    @Autowired
    private FlammaNewsService flammaNewsService;

    @Autowired
    private OpenUniversityNewsService openUniversityNewsService;

    @Autowired
    private GuideNewsService guideNewsService;

    @Autowired
    private SecurityUtils securityUtils;

    @Value("${newsfeeds.maxItems.student}")
    private int studentMaxNews;

    @Value("${newsfeeds.maxItems.teacher}")
    private int teacherMaxNews;

    @Value("${newsfeeds.studentFeedCategory}")
    private String studentFeedCategory;

    public List<NewsDto> getStudentNews(Locale locale) {
        Set<NewsDto> newsDtos = flammaNewsService.getStudentNews(locale).stream()
            .filter(item -> item.categories.contains(studentFeedCategory)).collect(toSet());

        List<NewsDto> guideNewsDtos = securityUtils.getAppUser()
            .map(AppUser::getStudentNumber)
            .map(sn -> guideNewsService.getGuideNewsForDegreeProgramme(sn.get(), locale))
            .orElse(new ArrayList<>());

        if (guideNewsDtos.isEmpty()) {
            guideNewsDtos = guideNewsService.getGuideNewsGeneral(locale);
        }
        newsDtos.addAll(guideNewsDtos);

        return newsDtos.stream()
            .sorted(Comparator.comparing(dto -> dto.updated, Comparator.reverseOrder()))
            .limit(studentMaxNews)
            .collect(Collectors.toList());
    }

    public List<NewsDto> getTeacherNews(Locale locale) {
        final List<NewsDto> newsDtos = flammaNewsService.getTeacherNews(locale);
        return newsDtos.subList(0, Math.min(teacherMaxNews, newsDtos.size()));
    }

    public List<NewsDto> getOpenUniversityNews() {
        final List<NewsDto> newsDtos = openUniversityNewsService.getOpenUniversityNews();
        return newsDtos.subList(0, Math.min(studentMaxNews, newsDtos.size()));
    }

}
