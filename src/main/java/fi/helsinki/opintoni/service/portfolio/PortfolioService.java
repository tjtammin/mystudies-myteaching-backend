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

package fi.helsinki.opintoni.service.portfolio;

import fi.helsinki.opintoni.domain.portfolio.ComponentVisibility;
import fi.helsinki.opintoni.domain.portfolio.Portfolio;
import fi.helsinki.opintoni.domain.portfolio.PortfolioVisibility;
import fi.helsinki.opintoni.domain.portfolio.TeacherPortfolioSection;
import fi.helsinki.opintoni.dto.portfolio.PortfolioDto;
import fi.helsinki.opintoni.exception.http.NotFoundException;
import fi.helsinki.opintoni.localization.Language;
import fi.helsinki.opintoni.repository.UserRepository;
import fi.helsinki.opintoni.repository.portfolio.PortfolioRepository;
import fi.helsinki.opintoni.service.converter.PortfolioConverter;
import fi.helsinki.opintoni.web.arguments.PortfolioRole;
import fi.helsinki.opintoni.web.rest.privateapi.portfolio.summary.UpdateSummaryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static fi.helsinki.opintoni.exception.http.NotFoundException.notFoundException;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final PortfolioPathGenerator portfolioPathGenerator;
    private final PortfolioConverter portfolioConverter;
    private final PortfolioStudyAttainmentWhitelistService whitelistService;
    private final ComponentVisibilityService componentVisibilityService;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository,
                            UserRepository userRepository,
                            PortfolioPathGenerator portfolioPathGenerator,
                            PortfolioConverter portfolioConverter,
                            PortfolioStudyAttainmentWhitelistService whitelistService,
                            ComponentVisibilityService componentVisibilityService) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.portfolioPathGenerator = portfolioPathGenerator;
        this.portfolioConverter = portfolioConverter;
        this.whitelistService = whitelistService;
        this.componentVisibilityService = componentVisibilityService;
    }

    public PortfolioDto insert(Long userId, String name, PortfolioRole portfolioRole, Language lang) {
        String portfolioPath = portfolioRepository
            .findByUserId(userId)
            .findFirst()
            .map(portfolio -> portfolio.path)
            .orElse(portfolioPathGenerator.create(name));

        Portfolio portfolio = new Portfolio();
        portfolio.language = lang;
        portfolio.user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        portfolio.path = portfolioPath;
        portfolio.ownerName = name;
        portfolio.visibility = PortfolioVisibility.PRIVATE;
        portfolio.portfolioRole = portfolioRole;
        Portfolio inserted = portfolioRepository.save(portfolio);

        if (portfolioRole == PortfolioRole.TEACHER) {
            insertTeacherPortfolioSectionVisibilities(portfolio);
        }

        whitelistService.insert(inserted);

        return portfolioConverter.toDto(inserted, PortfolioConverter.ComponentFetchStrategy.NONE);
    }

    public PortfolioDto get(Long userId, PortfolioRole portfolioRole) {
        return convertPortfolioToDto(
            portfolioRepository.findByUserIdAndPortfolioRole(userId, portfolioRole).findFirst(),
            PortfolioConverter.ComponentFetchStrategy.ALL);
    }

    public PortfolioDto findByPathAndLangAndRole(String path,
                                                 Language lang,
                                                 PortfolioRole portfolioRole,
                                                 PortfolioConverter.ComponentFetchStrategy componentFetchStrategy) {
        return convertPortfolioToDto(portfolioRepository
            .findByPathAndPortfolioRoleAndLanguage(path, portfolioRole, lang), componentFetchStrategy);
    }

    public PortfolioDto findById(Long portfolioId) {
        return convertPortfolioToDto(portfolioRepository
            .findById(portfolioId), PortfolioConverter.ComponentFetchStrategy.NONE);
    }

    private PortfolioDto convertPortfolioToDto(Optional<Portfolio> portfolioOptional,
                                               PortfolioConverter.ComponentFetchStrategy componentFetchStrategy) {
        return portfolioOptional
            .map((portfolio) -> portfolioConverter.toDto(portfolio, componentFetchStrategy))
            .orElseThrow(notFoundException("Portfolio not found"));
    }

    public Map<String, Map<String, List<String>>> getUserPortfolioPathsByRoleAndLang(Long userId) {
        return portfolioRepository
            .findByUserId(userId)
            .collect(groupingBy(
                portfolio -> portfolio.portfolioRole.getRole(),
                groupingBy(
                    portfolio -> portfolio.language.getCode(),
                    mapping(
                        PortfolioService::portfolioPath,
                        toList()
                    )
                )
            ));
    }

    public PortfolioDto update(Long portfolioId, PortfolioDto portfolioDto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(NotFoundException::new);
        portfolio.visibility = portfolioDto.visibility;
        portfolio.ownerName = portfolioDto.ownerName;
        portfolio.intro = portfolioDto.intro;

        return portfolioConverter.toDto(portfolioRepository.save(portfolio), PortfolioConverter.ComponentFetchStrategy.NONE);
    }

    public void updateSummary(Long portfolioId, UpdateSummaryRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(NotFoundException::new);
        portfolio.summary = request.summary;
        portfolioRepository.save(portfolio);
    }

    private void insertTeacherPortfolioSectionVisibilities(Portfolio portfolio) {
        List<ComponentVisibility> sectionVisibilities = Arrays.stream(TeacherPortfolioSection.values())
            .map(section -> {
                ComponentVisibility visibility = new ComponentVisibility();
                visibility.teacherPortfolioSection = section;
                visibility.visibility = section == TeacherPortfolioSection.BASIC_INFORMATION ?
                    ComponentVisibility.Visibility.PUBLIC :
                    ComponentVisibility.Visibility.PRIVATE;
                visibility.portfolio = portfolio;

                return visibility;
            }).collect(toList());

        componentVisibilityService.save(sectionVisibilities);
    }

    private static String portfolioPath(Portfolio portfolio) {
        return new StringJoiner("/", "/", "")
            .add(portfolio.language.getCode())
            .add(portfolio.path)
            .toString();
    }
}
