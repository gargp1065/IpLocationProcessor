package com.gl.eirs.iplocationprocessor.entity.app;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Data
@Table(name="ip_location_country_ipv6")
public class Ipv6 {

//    @Id
    public Ipv6(String[] parts) {
        this.startIpNumber = new BigInteger(parts[0]);
        this.endIpNumber = new BigInteger(parts[1]);
        this.countryCode = parts[2];
        this.countryName = parts[3];
        this.dataSource="File Dump";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_ip_number")
    BigInteger startIpNumber;


    @Column(name="end_ip_number")
    BigInteger endIpNumber;

    @Column(name="country_code")
    String countryCode;

    @Column(name="country_name")
    String countryName;

    @Column(name="data_source")
    String dataSource;

    public Ipv6() {

    }

//    @Column(name="asn")
//    String asn;
//
//    @Column(name="as")
//    String as;
//
//    @Column(name="region_name")
//    String regionName;
//
//    @Column(name="city_name")
//    String cityName;
//
//    @Column(name="timezone")
//    String timezone;
//
//    @Column(name="data_source")
//    String dataSource;
}
