package com.gl.eirs.iplocationprocessor.builder;



import com.gl.eirs.iplocationprocessor.entity.aud.ModulesAuditTrail;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Component
@Builder
public class ModulesAuditTrailBuilder {

public ModulesAuditTrail forInsert(Integer statusCode, String status,
                                          String errorMessage, String moduleName, String featureName,
                                          String action, String info, LocalDateTime startTime) {
    ModulesAuditTrail modulesAuditTrail = new ModulesAuditTrail();

    modulesAuditTrail.setCreatedOn(startTime);
    modulesAuditTrail.setModifiedOn(LocalDateTime.now());
    modulesAuditTrail.setExecutionTime(0);
    modulesAuditTrail.setStatusCode(statusCode);
    modulesAuditTrail.setStatus(status);
    modulesAuditTrail.setErrorMessage(errorMessage);
    modulesAuditTrail.setFeatureName(featureName);
    modulesAuditTrail.setModuleName(moduleName);
    modulesAuditTrail.setAction(action);
    modulesAuditTrail.setCount(0);
    modulesAuditTrail.setCount(0);
    modulesAuditTrail.setInfo(info);
    modulesAuditTrail.setServerName(getHostname());

    return modulesAuditTrail;
}

public static ModulesAuditTrail forUpdate(int id, Integer statusCode, String status,
                                          String errorMessage, String moduleName, String featureName,
                                          String action, String info, int count, int failureCount, long startTime) {
    ModulesAuditTrail modulesAuditTrail = new ModulesAuditTrail();

    modulesAuditTrail.setId(id);
//        modulesAuditTrail.setCreatedOn(startTime);
    modulesAuditTrail.setModifiedOn(LocalDateTime.now());
    modulesAuditTrail.setExecutionTime((int) (System.currentTimeMillis() - startTime));
    modulesAuditTrail.setStatusCode(statusCode);
    modulesAuditTrail.setStatus(status);
    modulesAuditTrail.setErrorMessage(errorMessage);
    modulesAuditTrail.setFeatureName(featureName);
    modulesAuditTrail.setModuleName(moduleName);
    modulesAuditTrail.setAction(action);
    modulesAuditTrail.setInfo(info);
    modulesAuditTrail.setCount(count);
    modulesAuditTrail.setFailureCount(failureCount);
    modulesAuditTrail.setServerName(getHostname());

    return modulesAuditTrail;
}
public static String getHostname() {
    try {
        InetAddress localHost = InetAddress.getLocalHost();
        return localHost.getHostName();
    } catch (Exception ex) {
        return "NA";
    }
}

}