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

package fi.helsinki.opintoni.web.rest.privateapi.portfolio;

import fi.helsinki.opintoni.SpringTest;
import fi.helsinki.opintoni.domain.portfolio.PortfolioBackground;
import fi.helsinki.opintoni.repository.portfolio.PortfolioBackgroundRepository;
import fi.helsinki.opintoni.web.rest.privateapi.usersettings.SelectBackgroundRequest;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;

import static fi.helsinki.opintoni.security.SecurityRequestPostProcessors.securityContext;
import static fi.helsinki.opintoni.security.TestSecurityContext.studentSecurityContext;
import static fi.helsinki.opintoni.web.WebTestUtils.toJsonBytes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PrivatePortfolioBackgroundResourceTest extends SpringTest {

    private static final String RESOURCE_URL = "/api/private/v1/portfolio/2/background";

    @Autowired
    private PortfolioBackgroundRepository portfolioBackgroundRepository;

    @Test
    public void thatBackgroundCanBeSelectedFromDefaults() throws Exception {
        SelectBackgroundRequest request = new SelectBackgroundRequest();
        request.filename = "Profile_1.jpg";

        mockMvc.perform(put(RESOURCE_URL + "/select")
            .with(securityContext(studentSecurityContext()))
            .content(toJsonBytes(request))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Optional<PortfolioBackground> background = portfolioBackgroundRepository.findByPortfolioId(2L);

        Assertions.assertThat(background.isPresent()).isTrue();
        Assertions.assertThat(background.get().backgroundFilename).isEqualTo(request.filename);
    }
}
