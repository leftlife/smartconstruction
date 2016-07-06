package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SmartBuildService {

    @GET("builds")
    Call<ArrayList<SmartBuild>> getSmartBuilds();

    @GET("builds/{build}")
    Call<SmartBuild> getSmartBuild(
            @Path("build") String buildCode
    );

}
