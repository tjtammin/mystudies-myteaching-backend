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

package fi.helsinki.opintoni.config.audit;

import fi.helsinki.opintoni.domain.PersistentAuditEvent;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Configuration
public class AuditEventConverter {

    /**
     * Convert a list of PersistentAuditEvent to a list of AuditEvent.
     *
     * @param persistentAuditEvents the list to convert
     * @return the converted list.
     */
    public List<AuditEvent> convertToAuditEvent(Iterable<PersistentAuditEvent> persistentAuditEvents) {
        if (persistentAuditEvents == null) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(persistentAuditEvents.spliterator(), false)
            .map(persistentAuditEvent -> new AuditEvent(
                persistentAuditEvent.getAuditEventDate().toDate().toInstant(),
                persistentAuditEvent.getPrincipal(),
                persistentAuditEvent.getAuditEventType(),
                convertDataToObjects(persistentAuditEvent.getData())))
            .collect(toList());
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator AuditEventRepository interface
     *
     * @param data the data to convert
     * @return a map of String, Object
     */
    private Map<String, Object> convertDataToObjects(Map<String, String> data) {
        Map<String, Object> results = new HashMap<>();

        if (data != null) {
            for (String key : data.keySet()) {
                results.put(key, data.get(key));
            }
        }

        return results;
    }

    /**
     * Internal conversion. This method will allow to save additional data.
     * By default, it will save the object as string
     *
     * @param data the data to convert
     * @return a map of String, String
     */
    public Map<String, String> convertDataToStrings(Map<String, Object> data) {
        Map<String, String> results = new HashMap<>();

        if (data != null) {
            for (String key : data.keySet()) {
                Object object = data.get(key);

                // Extract the data that will be saved.
                if (object instanceof WebAuthenticationDetails) {
                    WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) object;
                    results.put("remoteAddress", authenticationDetails.getRemoteAddress());
                    results.put("sessionId", authenticationDetails.getSessionId());
                } else {
                    results.put(key, object.toString());
                }
            }
        }

        return results;
    }
}
