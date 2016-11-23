package fi.helsinki.opintoni.task;

import fi.helsinki.opintoni.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty("coursePage.checkUpdates.interval.seconds")
public class CourseImplementationUpdateChecker {
    private CourseImplementationCacheBuster courseImplementationCacheBuster;

    private long updatesLastChecked;

    private static final Logger log = LoggerFactory.getLogger(CourseImplementationUpdateChecker.class);

    @Autowired
    public CourseImplementationUpdateChecker(CourseImplementationCacheBuster courseImplementationCacheBuster) {
        this.courseImplementationCacheBuster = courseImplementationCacheBuster;
        this.updatesLastChecked = jvmStartTimeInSeconds();
    }

    @Scheduled(fixedDelayString = "${coursePage.checkUpdates.interval.seconds}000")
    public void checkForUpdatedCourseImplementations() {
        long updatesSince = updatesLastChecked;
        updatesLastChecked = Instant.now().getEpochSecond();

        log.info("checking for course implementation updates since {}",
            LocalDateTime.ofInstant(Instant.ofEpochSecond(updatesSince), TimeService.HELSINKI_ZONE_ID));

        try {
            courseImplementationCacheBuster.checkForUpdatedCourseImplementations(updatesSince);
        } catch (RestClientException e) {
            log.error("checking for course implementation updates failed: {}", e.getMessage());
        }
    }

    private static long jvmStartTimeInSeconds() {
        return ManagementFactory.getRuntimeMXBean().getStartTime() / 1000;
    }
}