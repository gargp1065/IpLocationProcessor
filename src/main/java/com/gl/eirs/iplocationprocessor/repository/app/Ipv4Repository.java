package com.gl.eirs.iplocationprocessor.repository.app;

import com.gl.eirs.iplocationprocessor.entity.app.Ipv4;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface Ipv4Repository extends JpaRepository<Ipv4, Long> {

    @Transactional
    @Modifying
    @Query("delete from Ipv4 u where u.startIpNumber= :startIpNumber and u.endIpNumber= :endIpNumber and u.countryCode=:countryCode and u.countryName=:countryName")
    public void deleteByData(@Param("startIpNumber") Long startIpNumber, @Param("endIpNumber") Long endIpNumber,
                             @Param("countryCode") String countryCode, @Param("countryName") String countryName);


}

