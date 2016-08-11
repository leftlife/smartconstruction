package kr.koogle.android.smartconstruction.http;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LoginService {

    @FormUrlEncoded
    @POST("logins")
    Call<User> getLoginToken(@FieldMap Map<String, String> fields);

    @GET("logins/{token}")
    Call<User> checkLoginToken(
            @Path("token") String token
    );

    @FormUrlEncoded
    @POST("tokens")
    Call<AccessToken> getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType
    );

}