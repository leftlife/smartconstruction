package kr.koogle.android.smartconstruction.http;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by LeeSungWoo on 2016-06-21.
 */
public interface LoginService {

    @POST("login")
    Call<User> basicLogin();

    @FormUrlEncoded
    @POST("token")
    Call<AccessToken> getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType);

}