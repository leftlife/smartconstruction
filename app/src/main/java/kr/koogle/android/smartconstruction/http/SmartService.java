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


    Call<SmartWork> getSmartWorkView(
            @Path("build") String buildCode,
            @Path("work") String workCode
    );

    // 건축주 협의
    @GET("clients")
    Call<ArrayList<SmartBBSClient>> getSmartBBSClients();

    @GET("clients/{client}")
    Call<SmartBBSClient> getSmartBBSClient(
            @Path("client") String clientCode
    );

    // 작업지시
    @GET("orders")
    Call<ArrayList<SmartBBSOrder>> getSmartBBSOrders();

    @GET("orders/{order}")
    Call<SmartBBSOrder> getSmartBBSOrder(
            @Path("order") String orderCode
    );

    // 공종 카테고리
    @GET("labors")
    Call<ArrayList<SmartCategory>> getLaborCategorys();

    // 자재 카테고리
    @GET("meterials")
    Call<ArrayList<SmartCategory>> getMeterialCategorys();

    // 장비 카테고리
    @GET("equipments")
    Call<ArrayList<SmartCategory>> getEquipmentCategorys();

}
