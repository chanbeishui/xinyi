package com.xinyi.rongli.qualification.mapper;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.qualification.domain.QualificationApplication;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface QualificationMapper {
    DriverProfile lockProfile(long accountId);

    void insertProfile(DriverProfile profile);

    QualificationApplication current(long driverId);

    QualificationApplication lock(long id);

    void insert(QualificationApplication application);

    int save(QualificationApplication application);

    int submit(
            @Param("application") QualificationApplication application,
            @Param("profileVersion") long profileVersion);

    int updateInitialStatus(@Param("id") long id, @Param("status") String status);

    Map<String, Object> company(long id);

    List<Map<String, Object>> proofs(
            @Param("id") long id, @Param("revision") long revision, @Param("hash") byte[] hash);

    void insertSubmission(
            @Param("application") QualificationApplication application,
            @Param("profileVersion") long profileVersion,
            @Param("reason") byte[] reason);
}
