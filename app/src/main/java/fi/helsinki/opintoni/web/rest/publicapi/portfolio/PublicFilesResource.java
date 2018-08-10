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

package fi.helsinki.opintoni.web.rest.publicapi.portfolio;

import fi.helsinki.opintoni.integration.fileservice.FileServiceInOutStream;
import fi.helsinki.opintoni.service.portfolio.PortfolioFilesService;
import fi.helsinki.opintoni.web.rest.AbstractResource;
import fi.helsinki.opintoni.web.rest.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = RestConstants.PUBLIC_API_V1 + "/portfolio/files")
public class PublicFilesResource extends AbstractResource {

    private final PortfolioFilesService portfolioFilesService;

    @Autowired
    public PublicFilesResource(PortfolioFilesService portfolioFilesService) {
        this.portfolioFilesService = portfolioFilesService;
    }

    @GetMapping("/{portfolioName}/{filename:.+}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("portfolioName") String portfolioName,
                                                       @PathVariable("filename") String filename) {
        FileServiceInOutStream inOutStream = portfolioFilesService.getFile(String.join("/", portfolioName, filename));
        InputStreamResource isr = new InputStreamResource(inOutStream.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(inOutStream.getSize());
        return new ResponseEntity<>(isr, headers, HttpStatus.OK);
    }
}
