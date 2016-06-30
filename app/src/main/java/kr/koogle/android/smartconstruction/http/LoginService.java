package kr.koogle.android.smartconstruction.http;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by LeeSungWoo on 2016-06-21.
 */
public interface LoginService {

    @POST("login")
    Call<User> getLoginToken();

    @GET("login/{token}")
    Call<User> checkLoginToken(
            @Path("token") String token
    );

    @FormUrlEncoded
    @POST("token")
    Call<AccessToken> getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType);

}