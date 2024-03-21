package com.gl.eirs.iplocationprocessor.service;


import com.gl.eirs.iplocationprocessor.builder.ModulesAuditTrailBuilder;
import com.gl.eirs.iplocationprocessor.config.AppConfig;
import com.gl.eirs.iplocationprocessor.dto.FileDto;
import com.gl.eirs.iplocationprocessor.entity.aud.ModulesAuditTrail;
import com.gl.eirs.iplocationprocessor.repository.aud.ModulesAuditTrailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static com.gl.eirs.iplocationprocessor.constants.Constants.*;

@Service
public class MainService {


    @Autowired
    AppConfig appConfig;
    @Autowired
    ModulesAuditTrailRepository modulesAuditTrailRepository;

    @Autowired
    FileServiceIpV4 fileServiceIpV4;

    @Autowired
    FileServiceIpV6 fileServiceIpV6;

    @Autowired
    ModulesAuditTrailBuilder modulesAuditTrailBuilder;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public void processDeltaFiles() {


        String filePath = appConfig.getFilePath();
        String ipType = appConfig.getIpType();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String addFileName= "ip_location_country_add_"+ipType+"_diff_"+sdf.format(date).trim()+".csv";
        String delFileName= "ip_location_country_del_"+ipType+"_diff_"+sdf.format(date).trim()+".csv";

        FileDto addFileDto = new FileDto(addFileName, filePath);
        FileDto delFileDto = new FileDto(delFileName, filePath);

        int moduleAuditId ;
        long startTime = System.currentTimeMillis();
        ModulesAuditTrail modulesAuditTrail;
        modulesAuditTrail = modulesAuditTrailBuilder.forInsert(201, "INITIAL", "NA", moduleName, featureName + ipType, "", "", LocalDateTime.now());
        ModulesAuditTrail entity = modulesAuditTrailRepository.save(modulesAuditTrail);
        moduleAuditId = entity.getId();
        if(ipType.equalsIgnoreCase("ipv4")) {

            // create modules_audit_trail entry for this file.
            try {
                boolean delFileResponse = fileServiceIpV4.processDelFile(delFileDto);
                boolean addFileResponse = fileServiceIpV4.processAddFile(addFileDto);
            } catch (Exception ex) {
                logger.error("The file processing failed for ipv4 diff file");
                logger.info("Summary for add file {} is {}", addFileDto.getFileName(), addFileDto);
                logger.info("Summary for del file {} is {}", delFileDto.getFileName(), delFileDto);
                modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The file processing failed for ipv4 diff file",
                        (int) (addFileDto.getTotalRecords() + delFileDto.getTotalRecords()),
                        (int) (addFileDto.getFailedRecords() + delFileDto.getFailedRecords()),
                        (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) (addFileDto.getSuccessRecords() + delFileDto.getSuccessRecords()),
                        moduleAuditId);
                System.exit(1);
            }

        }
        else if(ipType.equalsIgnoreCase("ipv6")){

            // create modules_audit_trail entry for this file.


            try {
                boolean delFileResponse = fileServiceIpV6.processDelFile(delFileDto);
                boolean addFileResponse = fileServiceIpV6.processAddFile(addFileDto);
            } catch (Exception ex) {
                logger.error("The file processing failed for ipv6 diff file");
                logger.info("Summary for add file {} is {}", addFileDto.getFileName(), addFileDto);
                logger.info("Summary for del file {} is {}", delFileDto.getFileName(), delFileDto);
                modulesAuditTrailRepository.updateModulesAudit(501, "FAIL", "The file processing failed for ipv6 diff file",
                        (int) (addFileDto.getTotalRecords() + delFileDto.getTotalRecords()),
                        (int) (addFileDto.getFailedRecords() + delFileDto.getFailedRecords()),
                        (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) (addFileDto.getSuccessRecords() + delFileDto.getSuccessRecords()),
                        moduleAuditId);
                System.exit(1);
            }
        }

        logger.info("Summary for add file {} is {}", addFileDto.getFileName(), addFileDto);
        logger.info("Summary for del file {} is {}", delFileDto.getFileName(), delFileDto);

        modulesAuditTrailRepository.updateModulesAudit(200, "SUCCESS", "NA",
                (int) (addFileDto.getTotalRecords() + delFileDto.getTotalRecords()),
                (int) (addFileDto.getFailedRecords() + delFileDto.getFailedRecords()),
                (int) (System.currentTimeMillis() - startTime), LocalDateTime.now(), (int) (addFileDto.getSuccessRecords() + delFileDto.getSuccessRecords()),
                moduleAuditId);

    }


}
