package kr.koogle.android.smartconstruction.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface FileUploadService {

    @GET("upload/{intId}")
    Call<SmartPhoto> getUpload(
            @Path("intId") int intId
    );

    @Multipart
    @POST("upload")
    //Call<ResponseBody> upload(@Part("description") RequestBody description, @Part MultipartBody.Part file);
    Call<ResponseBody> upload(
            @PartMap Map<String, RequestBody> options,
            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadMulti(
            @PartMap Map<String, RequestBody> options,
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2
    );

    @Multipart
    @POST("upload/{intId}")
    Call<ResponseBody> modifyUpload(
            @Path("intId") int intId,
            @PartMap Map<String, RequestBody> options
    );

    @DELETE("upload/{intId}")
    Call<ResponseBody> deleteUpload(
            @Path("intId") int intId
    );

}