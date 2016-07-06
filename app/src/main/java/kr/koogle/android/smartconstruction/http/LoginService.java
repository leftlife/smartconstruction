package kr.koogle.android.smartconstruction.http;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LoginService {

    @POST("logins")
    Call<User> getLoginToken();

    @GET("logins/{token}")
    Call<User> checkLoginToken(
            @Path("token") String token
    );

    @FormUrlEncoded
    @POST("tokens")
    Call<AccessToken> getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType);

}