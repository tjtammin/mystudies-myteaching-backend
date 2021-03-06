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

package fi.helsinki.opintoni.service;

import fi.helsinki.opintoni.domain.UserSettings;
import fi.helsinki.opintoni.repository.UserSettingsRepository;
import fi.helsinki.opintoni.util.UriBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@Transactional
public class AvatarImageService {

    private final UserSettingsRepository userSettingsRepository;
    private final UriBuilder uriBuilder;

    @Autowired
    public AvatarImageService(UserSettingsRepository userSettingsRepository, UriBuilder uriBuilder) {
        this.userSettingsRepository = userSettingsRepository;
        this.uriBuilder = uriBuilder;
    }

    public String getAvatarImageUrl(Long userId) {
        return getAvatarImageUrl(uriBuilder::getDefaultUserAvatarUrl, userId);
    }

    private String getAvatarImageUrl(Supplier<String> defaultAvatarUrlSupplier, Long userId) {
        UserSettings userSettings = userSettingsRepository.findByUserId(userId);
        return userSettings.hasAvatarImage() ?
            uriBuilder.getUserAvatarUrlByOodiPersonId(userSettings.user.oodiPersonId) :
            defaultAvatarUrlSupplier.get();
    }

    public String getPortfolioAvatarImageUrl(Long userId) {
        return getAvatarImageUrl(uriBuilder::getPortfolioDefaultUserAvatarUrl, userId);
    }

}
