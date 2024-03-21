package com.gl.eirs.iplocationprocessor.repository.app;

import com.gl.eirs.iplocationprocessor.entity.app.Ipv6;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface Ipv6Repository extends JpaRepository<Ipv6, Long> {


    @Transactional
    @Modifying
    @Query("delete from Ipv6 u where u.startIpNumber= :startIpNumber and u.endIpNumber= :endIpNumber and u.countryCode=:countryCode and u.countryName=:countryName")
    public void deleteByData(@Param("startIpNumber") BigInteger startIpNumber, @Param("endIpNumber") BigInteger endIpNumber,
                               @Param("countryCode") String countryCode,@Param("countryName") String countryName);

//    @Transactional
//    @Modifying
//        @Query(value="insert into ip_location_country_ipv6 (start_ip_number, end_ip_number, country_code, country_name) values (:startIpNumber ,:endIpNumber ,:countryCode ,:countryName)", nativeQuery = true)
//    public void saveByData(@Param("startIpNumber") BigInteger startIpNumber, @Param("endIpNumber") BigInteger endIpNumber,
//                             @Param("countryCode") String countryCode,@Param("countryName") String countryName);



}
