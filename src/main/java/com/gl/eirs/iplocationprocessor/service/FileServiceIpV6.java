package com.gl.eirs.iplocationprocessor.service;

import com.gl.eirs.iplocationprocessor.config.AppConfig;
import com.gl.eirs.iplocationprocessor.dto.FileDto;
import com.gl.eirs.iplocationprocessor.entity.app.Ipv4;
import com.gl.eirs.iplocationprocessor.entity.app.Ipv6;
import com.gl.eirs.iplocationprocessor.repository.app.Ipv4Repository;
import com.gl.eirs.iplocationprocessor.repository.app.Ipv6Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
public class FileServiceIpV6 implements IFileService{

    @Autowired
    AppConfig appConfig;

    @Autowired
    Ipv4Repository ipv4Repository;

    @Autowired
    Ipv6Repository ipv6Repository;

//    @Autowired
//    IpRepository ipRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean processAddFile(FileDto fileDto, String errorFileName) {
        // read file and process the entries
        int failureCount=0;
        int succesCount=0;
        int emptyRecords=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {
            File outFile = new File(appConfig.getErrorFilePath() + "/" + errorFileName);
            PrintWriter writer = new PrintWriter(outFile);
            try {
                String record;
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        emptyRecords++;
                        continue;
                    }

                    String[] ipRecord = record.split(appConfig.getFileSeparator(), -1);
                    if(ipRecord.length !=4 ) {
                        logger.error("The record length is less than 4 {}", Arrays.stream(ipRecord).toList());
                        writer.println(record);
                        continue;
                    }
                    for (int i = 0; i < ipRecord.length; i++) {
                        ipRecord[i] = ipRecord[i].replaceAll("\"", "");
                    }
                    Ipv6 ip6 = new Ipv6(ipRecord);
                    try {
                        logger.info("Inserting the entry {}", ip6);
//                        ipv6Repository.saveByData(ip6.getStartIpNumber(), ip6.getEndIpNumber(), ip6.getCountryCode(), ip6.getCountryName());
                        ipv6Repository.save(ip6);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to save in Ipv6 table, {}", ip6);
                        failureCount++;
                    }
                }
                writer.close();
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
                fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
                return true;
            }

        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        } catch (IOException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        }
        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
        return false;
    }

    public boolean processDelFile(FileDto fileDto, String errorFileName) {
        int failureCount=0;
        int succesCount=0;
        int emptyRecords = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {
            File outFile = new File(appConfig.getErrorFilePath() + "/" + errorFileName);
            PrintWriter writer = new PrintWriter(outFile);
            try {
                String record;
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        emptyRecords++;
                        continue;
                    }

                    String[] ipRecord = record.split(appConfig.getFileSeparator(), -1);
                    if(ipRecord.length !=4 ) {
                        logger.error("The record length is less than 4 {}", Arrays.stream(ipRecord).toList());
                        writer.println(record);
                        continue;
                    }
                    for (int i = 0; i < ipRecord.length; i++) {
                        ipRecord[i] = ipRecord[i].replaceAll("\"", "");
                    }
                    Ipv6 ip6 = new Ipv6(ipRecord);
                    try {
                        logger.info("Deleting the entry {}", ip6);
                        ipv6Repository.deleteByData(ip6.getStartIpNumber(), ip6.getEndIpNumber(), ip6.getCountryCode(), ip6.getCountryName());
//                        ipv6Repository.delete(ip6);
                        succesCount++;
                    } catch (Exception ex) {
                        logger.error("The entry failed to delete in Ipv6 table, {}", ex);
                        failureCount++;
                    }
                }
                writer.close();
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
                fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
                return true;
            }
//            fileDto.setFailedRecords(failureCount);
//            fileDto.setSuccessRecords(succesCount);
        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        } catch (IOException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
            return true;
        }

        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        fileDto.setTotalRecords(fileDto.getTotalRecords() - emptyRecords);
        return false;
    }
}
