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

package fi.helsinki.opintoni.web.rest.restrictedapi.portfolio;

import fi.helsinki.opintoni.SpringTest;
import fi.helsinki.opintoni.domain.portfolio.ComponentVisibility;
import fi.helsinki.opintoni.domain.portfolio.Portfolio;
import fi.helsinki.opintoni.domain.portfolio.PortfolioComponent;
import fi.helsinki.opintoni.domain.portfolio.PortfolioVisibility;
import fi.helsinki.opintoni.repository.ComponentVisibilityRepository;
import fi.helsinki.opintoni.repository.portfolio.PortfolioRepository;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public abstract class RestrictedPortfolioTest extends SpringTest {

    protected static final String RESTRICTED_PORTFOLIO_API_PATH = "/api/restricted/v1/portfolio/2";

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ComponentVisibilityRepository componentVisibilityRepository;

    @Before
    public void savePortfolioAsRestricted() {
        Portfolio portfolio = portfolioRepository.findOne(2L);
        portfolio.visibility = PortfolioVisibility.RESTRICTED;
        portfolioRepository.save(portfolio);
    }

    public void setPrivateVisibilitiesForEveryComponent() {
        componentVisibilityRepository.deleteAll();

        Arrays.asList(PortfolioComponent.values()).forEach(component -> {
            ComponentVisibility componentVisibility = new ComponentVisibility();
            componentVisibility.component = component;
            componentVisibility.visibility = ComponentVisibility.Visibility.PRIVATE;
            componentVisibility.portfolio = portfolioRepository.findOne(2L);
            componentVisibilityRepository.save(componentVisibility);
        });
    }
}
