package com.example.ilya.lorekeep.topic.topicApi;

import com.example.ilya.lorekeep.topic.topicApi.models.TopicAnswer;
import com.example.ilya.lorekeep.topic.topicApi.models.TopicModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TopicApi {

    @POST("/api/topic")
    Call<TopicAnswer> createNewTopic(@Body TopicModel newTopic, @Header("Cookie") String sessionId);

    @PUT("/api/topic")
    Call<TopicAnswer> updateTopic(@Body TopicModel topic, @Header("Cookie") String sessionId);

    @GET("/api/topic/{userId}")
    Call<List<TopicModel>> getAllTopics(@Path("userId") int userId);

    @DELETE("/api/topic/{id}")
    Call<TopicAnswerDelModel> deleteTopic(@Path("id") int topicId, @Header("Cookie") String sessionId);

    @GET("/api/changes")
    Call<List<TopicModel>> getChanges(@Header("Cookie") String sessionId);

    @GET("/api/changes/delete/topic")
    Call<List<Integer>> getChagesDelTopic(@Header("Cookie") String sessionId);
}
