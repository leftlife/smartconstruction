package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface SmartBuildService {

    @GET("builds")
    Call<ArrayList<SmartBuild>> getSmartBuilds();

    @GET("builds/{build}")
    Call<SmartBuild> getSmartBuild(
            @Path("build") String buildCode
    );

    @GET("builds/{build}/works")
    Call<ArrayList<SmartWork>> getSmartWorks(
            @Path("build") String buildCode,
            @QueryMap Map<String, String> options
    );

    @GET("builds/{build}/works/{work}")
    Call<SmartWork> getSmartWorkView(
            @Path("build") String buildCode,
            @Path("work") String workCode
    );

}
