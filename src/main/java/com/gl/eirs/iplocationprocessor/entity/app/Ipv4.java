package com.gl.eirs.iplocationprocessor.entity.app;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="ip_location_country_ipv4")
public class Ipv4 {


//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_ip_number")
    long startIpNumber;

    @Column(name="end_ip_number")
    long endIpNumber;

    @Column(name="country_code")
    String countryCode;

    @Column(name="country_name")
    String countryName;

    @Column(name="data_source")
    String dataSource;
//
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

    public Ipv4(String[] parts) {
        this.startIpNumber = Long.parseLong(parts[0]);
        this.endIpNumber = Long.parseLong(parts[1]);
        this.countryCode = parts[2];
        this.countryName = parts[3];
        this.dataSource = "File Dump";
    }

    public Ipv4() {

    }
}
