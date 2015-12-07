package fi.helsinki.opintoni.integration.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.Optional;

public class FeedMockClient implements FeedClient {

    @Value("classpath:sampledata/feed/feed.rss")
    private Resource mockFeed;

    private final ObjectMapper objectMapper;

    @Autowired
    public FeedMockClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<SyndFeed> getFeed(String feedUrl) {
        Optional<SyndFeed> feed = Optional.empty();
        try {
            SyndFeedInput input = new SyndFeedInput();
            feed = Optional.ofNullable(input.build(new XmlReader(mockFeed.getInputStream())));
        } catch (Exception e) {
        }
        return feed;
    }
}