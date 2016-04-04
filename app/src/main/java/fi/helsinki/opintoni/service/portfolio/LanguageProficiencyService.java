package fi.helsinki.opintoni.service.portfolio;

import fi.helsinki.opintoni.domain.portfolio.PortfolioLanguageProficiency;
import fi.helsinki.opintoni.dto.portfolio.LanguageProficienciesChangeDescriptorDto;
import fi.helsinki.opintoni.dto.portfolio.LanguageProficiencyDto;
import fi.helsinki.opintoni.repository.portfolio.LanguageProficiencyRepository;
import fi.helsinki.opintoni.repository.portfolio.PortfolioRepository;
import fi.helsinki.opintoni.security.authorization.PermissionChecker;
import fi.helsinki.opintoni.service.converter.portfolio.LanguageProficiencyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LanguageProficiencyService {

    private final PortfolioRepository portfolioRepository;
    private final LanguageProficiencyRepository languageProficiencyRepository;
    private final LanguageProficiencyConverter languageProficiencyConverter;
    private final PermissionChecker permissionChecker;

    @Autowired
    public LanguageProficiencyService(PortfolioRepository portfolioRepository,
                                      LanguageProficiencyRepository languageProficiencyRepository,
                                      LanguageProficiencyConverter languageProficiencyConverter,
                                      PermissionChecker permissionChecker) {
        this.portfolioRepository = portfolioRepository;
        this.languageProficiencyRepository = languageProficiencyRepository;
        this.languageProficiencyConverter = languageProficiencyConverter;
        this.permissionChecker = permissionChecker;
    }

    public List<LanguageProficiencyDto> findByPortfolioId(Long id) {
        return languageProficiencyRepository.findByPortfolioId(id).stream()
            .map(languageProficiencyConverter::toDto)
            .collect(Collectors.toList());
    }

    public void updateLanguageProficiencies(LanguageProficienciesChangeDescriptorDto changeDescriptor,
                                            Long portfolioId,
                                            Long userId) {
        if (changeDescriptor.deletedIds != null) {
            changeDescriptor.deletedIds.forEach(id -> deleteLanguageProficiency(id, userId));
        }

        if(changeDescriptor.updatedLanguageProficiencies != null) {
            changeDescriptor.updatedLanguageProficiencies.forEach((dto) -> updateLanguageProficiency(dto, userId));
        }

        if(changeDescriptor.newLanguageProficiencies != null) {
            changeDescriptor.newLanguageProficiencies.forEach((dto) -> addLanguageProficiency(portfolioId, dto));
        }
    }

    private void addLanguageProficiency(Long portfolioId, LanguageProficiencyDto languageProficiencyDto) {
        PortfolioLanguageProficiency portfolioLanguageProficiency = new PortfolioLanguageProficiency();
        portfolioLanguageProficiency.languageCode = languageProficiencyDto.language;
        portfolioLanguageProficiency.proficiency = languageProficiencyDto.proficiency;
        portfolioLanguageProficiency.portfolio = portfolioRepository.findOne(portfolioId);

        languageProficiencyRepository.save(portfolioLanguageProficiency);
    }

    private void updateLanguageProficiency(LanguageProficiencyDto languageProficiencyDto, Long userId) {
        validateOwnership(userId, languageProficiencyDto.id);
        PortfolioLanguageProficiency languageProficiency =
            languageProficiencyRepository.findOne(languageProficiencyDto.id);
        languageProficiency.languageCode= languageProficiencyDto.language;
        languageProficiency.proficiency = languageProficiencyDto.proficiency;
    }

    private void deleteLanguageProficiency(Long languageProficiencyId, Long userId) {
        validateOwnership(userId, languageProficiencyId);
        languageProficiencyRepository.delete(languageProficiencyId);
    }

    private void validateOwnership(Long userId, Long languageProficiencyId) {
        permissionChecker.verifyPermission(userId, languageProficiencyId, PortfolioLanguageProficiency.class);
    }
}