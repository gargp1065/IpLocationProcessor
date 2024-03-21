package com.gl.eirs.iplocationprocessor.service;


import com.gl.eirs.iplocationprocessor.config.AppConfig;
import com.gl.eirs.iplocationprocessor.dto.FileDto;
import com.gl.eirs.iplocationprocessor.entity.app.Ipv4;
import com.gl.eirs.iplocationprocessor.repository.app.Ipv4Repository;
import com.gl.eirs.iplocationprocessor.repository.app.Ipv6Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@Service
public class FileServiceIpV4 implements IFileService{


    @Autowired
    AppConfig appConfig;

    @Autowired
    Ipv4Repository ipv4Repository;

    @Autowired
    Ipv6Repository ipv6Repository;

//    @Autowired
//    IpRepository ipRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean processAddFile(FileDto fileDto) {
        // read file and process the entries
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {

            try {
                String record;
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        continue;
                    }

                    String[] ipRecord = record.split(appConfig.getFileSeparator(), -1);
                    if(ipRecord.length < 4) {
                        logger.error("The record length is less than 4 {}", Arrays.stream(ipRecord).toList());
                        continue;
                    }
                    for (int i = 0; i < ipRecord.length; i++) {
                        ipRecord[i] = ipRecord[i].replaceAll("\"", "");
                    }
                    Ipv4 ip4 = new Ipv4(ipRecord);
                    try {
                        logger.info("Inserting the entry {}", ip4);
                        ipv4Repository.save(ip4);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to save in Ipv4 table, {}", ip4);
                        failureCount++;
                    }
                }
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
            }

        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        } catch (IOException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        }
        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        return false;
    }

    public boolean processDelFile(FileDto fileDto) {
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {

            try {
                String record;
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        continue;
                    }

                    String[] ipRecord = record.split(appConfig.getFileSeparator(), -1);
                    if(ipRecord.length < 4) {
                        logger.error("The record length is less than 4 {}", Arrays.stream(ipRecord).toList());
                        continue;
                    }
                    for (int i = 0; i < ipRecord.length; i++) {
                        ipRecord[i] = ipRecord[i].replaceAll("\"", "");
                    }
                    Ipv4 ip4 = new Ipv4(ipRecord);
                    try {
                        logger.info("Deleting the entry {}", ip4);
                        ipv4Repository.deleteByData(ip4.getStartIpNumber(), ip4.getEndIpNumber(), ip4.getCountryCode(), ip4.getCountryName());
//                        ipv4Repository.delete(ip4);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to delete in Ipv4 table, {}", ip4);
                        failureCount++;
                    }
                }
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
            }

        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        } catch (IOException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
        }
        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        return false;
    }
}
