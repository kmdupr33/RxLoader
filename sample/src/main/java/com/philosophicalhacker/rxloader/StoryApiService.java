package com.philosophicalhacker.rxloader;

import io.reactivex.Observable;
import java.util.List;
import retrofit2.http.GET;

interface StoryApiService {
  @GET("topstories.json") Observable<List<Integer>> getStories();

  @GET("askstories.json") Observable<List<Integer>> getAskStories();
}
