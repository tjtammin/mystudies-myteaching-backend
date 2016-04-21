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

package fi.helsinki.opintoni.integration.leiki;

import com.google.common.collect.Lists;
import fi.helsinki.opintoni.config.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;

public class LeikiRestClient implements LeikiClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(LeikiRestClient.class);

    private final String baseUrl;
    private final int maxSearchResults;
    private final int maxCategoryResults;
    private final int maxRecommendations;
    private final String recommendationUidPrefix;
    private final RestTemplate restTemplate;

    public LeikiRestClient(AppConfiguration appConfiguration, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseUrl = appConfiguration.get("leiki.base.url");
        this.maxSearchResults = appConfiguration.getInteger("search.maxSearchResults");
        this.maxCategoryResults = appConfiguration.getInteger("search.maxCategoryResults");
        this.maxRecommendations = appConfiguration.getInteger("recommendations.maxRecommendations");
        this.recommendationUidPrefix = appConfiguration.get("recommendations.uidPrefix");
    }

    @Override
    public List<LeikiSearchHit> search(String searchTerm, Locale locale) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/focus/api")
            .queryParam("method", "searchc")
            .query("sourceallmatch")
            .query("instancesearch")
            .queryParam("lang", locale.getLanguage())
            .queryParam("query", searchTerm)
            .queryParam("format", "json")
            .queryParam("t_type", LeikiTType.getByLocale(locale).getValue())
            .queryParam("max", maxSearchResults)
            .queryParam("fulltext", "true")
            .queryParam("partialsearchpriority", "ontology").build().encode().toUri();

        return getLeikiItemsData(uri, new ParameterizedTypeReference<LeikiResponse<LeikiSearchHit>>() {
        });
    }

    @Override
    public List<LeikiCategoryHit> searchCategory(String searchTerm, Locale locale) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/focus/api")
            .queryParam("method", "searchcategories")
            .queryParam("autocomplete", "occurred")
            .queryParam("lang", locale.getLanguage())
            .queryParam("text", searchTerm)
            .queryParam("format", "json")
            .queryParam("max", maxCategoryResults).build().encode().toUri();

        return getLeikiCategoryData(uri, new ParameterizedTypeReference<LeikiCategoryResponse<LeikiCategoryHit>>() {
        });
    }

    @Override
    public List<LeikiCourseRecommendation> getCourseRecommendations(String studentNumber) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/focus/api")
            .queryParam("method", "getsocial")
            .queryParam("similaritylimit", "99")
            .queryParam("format", "json")
            .queryParam("t_type", "kurssit")
            .queryParam("max", maxRecommendations)
            .queryParam("uid", recommendationUidPrefix + studentNumber)
            .queryParam("showtags", "true")
            .queryParam("unreadonly", "true")
            .build()
            .encode()
            .toUri();

        try {
            return getLeikiItemsData(uri, new ParameterizedTypeReference<LeikiResponse<LeikiCourseRecommendation>>() {});
        } catch(Exception e) {
            LOGGER.error("Caught exception when fetching Leiki course recommendations", e);
            return Lists.newArrayList();
        }
    }

    public <T> List<T> getLeikiItemsData(URI uri,
                                         ParameterizedTypeReference<LeikiResponse<T>> typeReference) {
        LeikiData<T> leikiData = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            typeReference).getBody().data;

        return leikiData.items != null ? leikiData.items : Lists.newArrayList();
    }

    public <T> List<T> getLeikiCategoryData(URI uri,
                                            ParameterizedTypeReference<LeikiCategoryResponse<T>> typeReference) {
        LeikiCategoryData<T> leikiCategoryData = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            typeReference).getBody().data;

        return leikiCategoryData.matches.get(0).match;
    }
}
