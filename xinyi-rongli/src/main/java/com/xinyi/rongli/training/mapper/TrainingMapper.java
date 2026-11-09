package com.xinyi.rongli.training.mapper;

import com.xinyi.rongli.training.domain.TrainingCourse;
import com.xinyi.rongli.training.dto.TrainingRequests;
import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface TrainingMapper {
    TrainingCourse find(long id);

    Map<String, Object> company(long id);

    TrainingCourse lock(long id);

    TrainingCourse lockFamily(String key);

    TrainingCourse draft(String key);

    int maxVersion(String key);

    List<TrainingCourse> list(
            @Param("scope") Set<Long> scope,
            @Param("query") TrainingRequests.Query query,
            @Param("offset") long offset);

    long count(@Param("scope") Set<Long> scope, @Param("query") TrainingRequests.Query query);

    List<Map<String, Object>> questions(long id);

    int questionCount(long id);

    void insert(TrainingCourse row);

    int save(TrainingCourse row);

    int publish(
            @Param("id") long id,
            @Param("version") long version,
            @Param("userId") long userId,
            @Param("hash") byte[] hash);

    void clearQuestions(long id);

    void insertQuestion(
            @Param("trainingId") long trainingId,
            @Param("question") TrainingRequests.Question question,
            @Param("options") String options,
            @Param("answers") String answers,
            @Param("sort") int sort);
}
