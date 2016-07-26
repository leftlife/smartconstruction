package kr.koogle.android.smartconstruction.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface FileUploadService {
    @Multipart
    @POST("upload")
    //Call<ResponseBody> upload(@Part("description") RequestBody description, @Part MultipartBody.Part file);
    Call<ResponseBody> upload(@PartMap Map<String, RequestBody> options, @Part MultipartBody.Part file);
}