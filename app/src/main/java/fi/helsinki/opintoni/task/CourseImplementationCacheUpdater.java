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

package fi.helsinki.opintoni.task;

import fi.helsinki.opintoni.domain.CachedItemUpdatesCheck;
import fi.helsinki.opintoni.integration.coursepage.CoursePageClient;
import fi.helsinki.opintoni.repository.CachedItemUpdatesCheckRepository;
import fi.helsinki.opintoni.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static fi.helsinki.opintoni.cache.CacheConstants.COURSE_PAGE;
import static java.util.Arrays.asList;

@Component
public class CourseImplementationCacheUpdater {

    @Value("${locale.available}")
    private String[] availableLocales;

    private final CoursePageClient coursePageClient;
    private final CachedItemUpdatesCheckRepository cachedItemUpdatesCheckRepository;
    private final CacheManager persistentCacheManager;

    private static final Logger log = LoggerFactory.getLogger(CourseImplementationCacheUpdater.class);

    @Autowired
    public CourseImplementationCacheUpdater(CoursePageClient coursePageClient,
                                            CachedItemUpdatesCheckRepository cachedItemUpdatesCheckRepository,
                                            CacheManager persistentCacheManager) {
        this.coursePageClient = coursePageClient;
        this.cachedItemUpdatesCheckRepository = cachedItemUpdatesCheckRepository;
        this.persistentCacheManager = persistentCacheManager;
    }

    public void getUpdatedCourseImplementationsAndEvictFromCache() {

        CachedItemUpdatesCheck cachedItemUpdatesCheck = cachedItemUpdatesCheckRepository.findByCacheName(COURSE_PAGE)
            .orElseGet(this::initialCourseImplementationUpdatesCheck);

        log.info("checking for course implementation updates since {}", cachedItemUpdatesCheck.dateTime);

        try {
            LocalDateTime updateCheckDateTime = LocalDateTime.now();

            List<Long> updatedCourses =
                coursePageClient.getUpdatedCourseImplementationIds(
                    cachedItemUpdatesCheck.dateTime.atZone(TimeService.HELSINKI_ZONE_ID).toEpochSecond());

            updateCachedCourses(updatedCourses);

            cachedItemUpdatesCheck.dateTime = updateCheckDateTime;

            cachedItemUpdatesCheckRepository.save(cachedItemUpdatesCheck);
        } catch (RestClientException e) {
            log.error("checking for course implementation updates failed: {}", e.getMessage());
        }
    }

    private CachedItemUpdatesCheck initialCourseImplementationUpdatesCheck() {
        CachedItemUpdatesCheck cachedItemUpdatesCheck = new CachedItemUpdatesCheck();
        cachedItemUpdatesCheck.cacheName = COURSE_PAGE;
        cachedItemUpdatesCheck.dateTime = initialCourseImplementationUpdateCheckDateTime();
        return cachedItemUpdatesCheck;
    }

    private LocalDateTime initialCourseImplementationUpdateCheckDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jvmStartTime()), TimeService.HELSINKI_ZONE_ID).minusHours(1);
    }

    private long jvmStartTime() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    private void updateCachedCourses(List<Long> updatedCourses) {
        Cache courseImplementationCache = persistentCacheManager.getCache(COURSE_PAGE);
        updatedCourses.stream().forEach(courseId ->
            asList(availableLocales).stream()
                .map(Locale::new)
                .forEach(locale -> {
                    String cacheKey = String.format("%s_%s", courseId, locale);

                    if(courseImplementationCache.get(cacheKey) != null) {
                        log.trace("Updating cache entry for course impl with key {}", cacheKey);

                        courseImplementationCache.evict(cacheKey);
                        coursePageClient.getCoursePage(courseId.toString(), locale);
                    }
                }));
    }

}
