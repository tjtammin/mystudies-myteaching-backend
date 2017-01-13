package fi.helsinki.opintoni.integration.esb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class ESBMockClient implements ESBClient {

    @Value("classpath:sampledata/esb/employeeinfo.json")
    private Resource employeeInfo;

    private final ObjectMapper objectMapper;

    public ESBMockClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ESBEmployeeInfo> getEmployeeInfo(String employeeNumber) {
        try {
            return objectMapper.readValue(employeeInfo.getInputStream(), new TypeReference<List<ESBEmployeeInfo>>() {});
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
