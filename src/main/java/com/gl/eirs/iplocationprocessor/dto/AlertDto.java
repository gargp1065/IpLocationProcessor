package com.gl.eirs.iplocationprocessor.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {

    private String alertId;

    private String alertMessage;

    private String alertProcess;

    private String userId;
}
