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

package fi.helsinki.opintoni.web.rest.privateapi.portfolio.background;

import fi.helsinki.opintoni.domain.portfolio.Portfolio;
import fi.helsinki.opintoni.dto.portfolio.PortfolioBackgroundDto;
import fi.helsinki.opintoni.security.authorization.PermissionChecker;
import fi.helsinki.opintoni.service.portfolio.PortfolioBackgroundService;
import fi.helsinki.opintoni.web.WebConstants;
import fi.helsinki.opintoni.web.arguments.UserId;
import fi.helsinki.opintoni.web.rest.AbstractResource;
import fi.helsinki.opintoni.web.rest.RestConstants;
import fi.helsinki.opintoni.web.rest.privateapi.usersettings.SelectBackgroundRequest;
import fi.helsinki.opintoni.web.rest.privateapi.usersettings.UploadImageBase64Request;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = RestConstants.PRIVATE_API_V1 + "/portfolio/{portfolioId:" + RestConstants.MATCH_NUMBER + "}/background",
    produces = WebConstants.APPLICATION_JSON_UTF8
)
public class PortfolioBackgroundResource extends AbstractResource {

    private final PortfolioBackgroundService portfolioBackgroundService;
    private final PermissionChecker permissionChecker;

    public PortfolioBackgroundResource(PortfolioBackgroundService portfolioBackgroundService,
                                       PermissionChecker permissionChecker) {
        this.portfolioBackgroundService = portfolioBackgroundService;
        this.permissionChecker = permissionChecker;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.PUT)
    public ResponseEntity<Boolean> uploadBackground(@UserId Long userId,
                                                    @PathVariable Long portfolioId,
                                                    @RequestBody UploadImageBase64Request request) {
        permissionChecker.hasPermission(userId, portfolioId, Portfolio.class);
        portfolioBackgroundService.uploadBackground(portfolioId, request);
        return response(true);
    }

    @RequestMapping(value = "/select", method = RequestMethod.PUT)
    public ResponseEntity<Boolean> selectBackground(@UserId Long userId,
                                                    @PathVariable Long portfolioId,
                                                    @RequestBody SelectBackgroundRequest request) {
        permissionChecker.hasPermission(userId, portfolioId, Portfolio.class);
        portfolioBackgroundService.selectBackground(portfolioId, request);
        return response(true);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<PortfolioBackgroundDto> getPortfolioBackgroundUri(@UserId Long userId,
                                                                            @PathVariable Long portfolioId) {
        permissionChecker.hasPermission(userId, portfolioId, Portfolio.class);
        String portfolioBackgroundUri = portfolioBackgroundService.getPortfolioBackgroundUri(portfolioId);
        return response(new PortfolioBackgroundDto(portfolioBackgroundUri));
    }
}
