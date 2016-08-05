package kr.koogle.android.smartconstruction.http;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface SmartService {

    // 진행중인 현장
    @GET("builds")
    Call<ArrayList<SmartBuild>> getSmartBuilds();

    @GET("builds/{build}")
    Call<SmartBuild> getSmartBuild(
        @Path("build") String buildCode
    );

    // 스마트 일보
    @GET("builds/{build}/works")
    Call<ArrayList<SmartWork>> getSmartWorks(
        @Path("build") String buildCode,
        @QueryMap Map<String, String> options
    );

    @GET("builds/{build}/works/{work}")
    Call<SmartWork> getSmartWork(
        @Path("build") String buildCode,
        @Path("work") String workCode
    );

    // 건축주 협의
    @GET("clients")
    Call<ArrayList<SmartClient>> getSmartBBSClients();

    @GET("clients/{client}")
    Call<SmartClient> getSmartBBSClient(
        @Path("client") String clientCode
    );

    @FormUrlEncoded
    @POST("clients")
    Call<ResponseBody> registClient(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("clients/{client}")
    Call<ResponseBody> modifyClient(
        @Path("client") String clientCode,
        @FieldMap Map<String, String> fields
    );

    @DELETE("clients/{client}")
    Call<ResponseBody> deleteClient(
        @Path("client") String clientCode
    );

    // 작업지시
    @GET("orders")
    Call<ArrayList<SmartOrder>> getSmartBBSOrders();

    @GET("orders/{order}")
    Call<SmartOrder> getSmartBBSOrder(
        @Path("order") String orderCode
    );

    @FormUrlEncoded
    @POST("orders")
    Call<ResponseBody> registOrder(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("orders/{order}")
    Call<ResponseBody> modifyOrder(
        @Path("order") String orderCode,
        @FieldMap Map<String, String> fields
    );

    @DELETE("orders/{order}")
    Call<ResponseBody> deleteOrder(
        @Path("order") String orderCode
    );

    // 사진관리
    @GET("photos")
    Call<ArrayList<SmartPhoto>> getSmartPhotos(
        @QueryMap Map<String, String> options
    );

    @GET("photos/{photo}")
    Call<SmartPhoto> getSmartPhoto(
        @Path("photo") String photoCode,
        @QueryMap Map<String, String> options
    );

    // 답글 업로드
    @FormUrlEncoded
    @POST("comments")
    Call<ResponseBody> registComment(@FieldMap Map<String, String> fields);

    @DELETE("comments/{comment}")
    Call<ResponseBody> deleteComment(
        @Path("comment") String commentCode
    );

    // 공종 카테고리
    @GET("labors")
    Call<ArrayList<SmartCategory>> getLaborCategorys();

    // 자재 카테고리
    @GET("materials")
    Call<ArrayList<SmartCategory>> getMaterialCategorys();

    // 장비 카테고리
    @GET("equipments")
    Call<ArrayList<SmartCategory>> getEquipmentCategorys();

    // 사원목록
    @GET("employees")
    Call<ArrayList<SmartEmployee>> getSmartEmployees();

}
