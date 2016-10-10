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

import fi.helsinki.opintoni.integration.pagemetadata.PageMetaData;
import fi.helsinki.opintoni.integration.pagemetadata.PageMetaDataClient;
import fi.helsinki.opintoni.web.WebConstants;
import fi.helsinki.opintoni.web.rest.AbstractResource;
import fi.helsinki.opintoni.web.rest.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = RestConstants.PRIVATE_API_V1,
    produces = WebConstants.APPLICATION_JSON_UTF8)
public class PageMetaDataResource extends AbstractResource {

    private final PageMetaDataClient pageMetaDataClient;

    @Autowired
    public PageMetaDataResource(PageMetaDataClient pageMetaDataClient) {
        this.pageMetaDataClient = pageMetaDataClient;
    }

    @RequestMapping(value = "/page-meta-data", method = RequestMethod.GET)
    public PageMetaData getPageMetaData(@RequestParam("url") String url) {
        return pageMetaDataClient.getPageMetaData(url);
    }

}
