package com.gl.eirs.iplocationprocessor.entity.aud;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "modules_audit_trail")
public class ModulesAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @Column(name = "execution_time")
    private Integer executionTime;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "feature_name")
    private String featureName;

    @Column(name = "action")
    private String action;

    @Column(name = "count")
    private Integer count;

    @Column(name = "info")
    private String info;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "count2")
    private Integer count2;

    @Column(name = "failure_count")
    private Integer failureCount;


    public ModulesAuditTrail() {
    }

    public ModulesAuditTrail(int id, LocalDateTime createdOn, LocalDateTime modifiedOn, Integer executionTime, Integer statusCode, String status, String errorMessage, String moduleName, String featureName, String action, Integer count, String info, String serverName, Integer count2, Integer failureCount) {
        this.id = id;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.executionTime = executionTime;
        this.statusCode = statusCode;
        this.status = status;
        this.errorMessage = errorMessage;
        this.moduleName = moduleName;
        this.featureName = featureName;
        this.action = action;
        this.count = count;
        this.info = info;
        this.serverName = serverName;
        this.count2 = count2;
        this.failureCount = failureCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModulesAuditTrail{");
        sb.append("id=").append(id);
        sb.append(", createdOn=").append(createdOn);
        sb.append(", modifiedOn=").append(modifiedOn);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", status='").append(status).append('\'');
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append(", moduleName='").append(moduleName).append('\'');
        sb.append(", featureName='").append(featureName).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", count=").append(count);
        sb.append(", info='").append(info).append('\'');
        sb.append(", serverName='").append(serverName).append('\'');
        sb.append(", count2=").append(count2);
        sb.append(", failureCount=").append(failureCount);
        sb.append('}');
        return sb.toString();
    }
}

