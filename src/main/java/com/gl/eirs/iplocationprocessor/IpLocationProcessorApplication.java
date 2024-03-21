package com.gl.eirs.iplocationprocessor;

import com.gl.eirs.iplocationprocessor.service.MainService;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class IpLocationProcessorApplication implements CommandLineRunner {

    @Autowired
    MainService mainService;
    public static void main(String[] args) {
        SpringApplication.run(IpLocationProcessorApplication.class, args);
    }


    public static void run() {
        ProcessBuilder builder = new ProcessBuilder();

    }


    @Override
    public void run(String... args) throws Exception {
        mainService.processDeltaFiles();
    }
}
