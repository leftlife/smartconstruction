package kr.koogle.android.smartconstruction;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import kr.koogle.android.smartconstruction.http.FileUploadService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by LeeSungWoo on 2016-06-28.
 */
public class OneFragment extends Fragment {
    private static final int REQ_CODE_PICK_IMAGE = 1001;
    private View rootView;

    //  ############## Fragment 통신 ##################  //
    private OnHeadlineSelectedListener customListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Framgment 통신 사용 !!! -> Activity
        //customListener.onArticleSelected(1);

        Button loginButton = (Button) rootView.findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(ServiceGenerator.API_BASE_URL + "/login" + "?client_id=" + "id" + "&redirect_uri=" + "Uri"));
                startActivity(intent);
            }
        });

        Button uploadButton = (Button) rootView.findViewById(R.id.btn_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                /*
                // crop된 기능 추가!!
                File tempFile = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
                Uri tempUri = Uri.fromFile(tempFile);
                intent.putExtra("crop", "true");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                */

                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
            }
        });


        // picasso 사용예
        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView1);
        Picasso.with(getContext())
                .load("https://tpc.googlesyndication.com/simgad/11582263531426008338")
                .placeholder(R.drawable.common_full_open_on_phone)
                .error(R.drawable.common_plus_signin_btn_icon_dark)
                .resize(100,100) // fit() 알아서 용량 줄여서 가져옴.  // centerCrop()
                .rotate(90)
                .into(imageView);

        return rootView;
    }

    //  ############## Fragment 통신 ##################  //
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activity(MainActivity)가 onSelectedListener를 구현했는지 확인 !!
        try {
            customListener = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnHeadlineSelectedListener");
        }
    }
    //  ############## Fragment 통신 ##################  // 이벤트 콜백 인터페이스 구현 !!
    public interface OnHeadlineSelectedListener {
        void onArticleSelected(int position);
    }

    // Activity -> OneFragment 로 전달되는 함수
    public void updateArticleView(int position) {

    }

    // 부모 Activity 가져오기
    Activity parentActiivty = getActivity();

    // 저장한 bundle 가져올때
    Bundle extra = getArguments();
    //String strId = extra.getString("strId");



    // 자식 엑티비티에서 결과값 받아 처리하기
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode, Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == getActivity().RESULT_OK) {
                    try {
                        // Uri에서 이미지 이름을 얻어온다.
                        String name_Str = getImageNameToUri(data.getData());

                        // 이미지 데이터를 비트맵으로 받아온다.
                        Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                        ImageView image = (ImageView) rootView.findViewById(R.id.imageView1);

                        // 배치해놓은 ImageView에 set
                        image.setImageBitmap(image_bitmap);


                        final Uri selectImageUri = data.getData();
                        final String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        final Cursor imageCursor = getContext().getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
                        imageCursor.moveToFirst();

                        final int columnIndex = imageCursor.getColumnIndex(filePathColumn[0]);
                        final String imagePath = imageCursor.getString(columnIndex);
                        imageCursor.close();
                        final Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                        // 서버에 업로드
                        File fileImage = new File(imagePath);
                        uploadFile(fileImage);

                        //Toast.makeText(getBaseContext(), "name_Str + " + ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) , Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (data != null) {
                        //String filePath = getExternalFilesDir(null).getAbsolutePath() + "/" + TEMP_PHOTO_FILE;
                        //LogUtil.Logd("Profile Image Path", filePath);

                        // File 객체로 만들어 byte[]로 변환 후 멀티파트를 이용해서 사진을 서버에 전송한다.
                        //sendUpdateProfileImage(CommUtil.convertFileToByteArray(new File(filePath)));

                        // 리턴받은 이미지 경로를 이용해서 비트맵으로 변환
                        //Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    }
                }
                break;
        }
    }


    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }

    public void uploadFile(File fileImage) {
        // create upload service client
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = fileImage; //FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        //Toast.makeText(getBaseContext(), "filename : " + file.getName() , Toast.LENGTH_SHORT).show();
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("userfile1", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = service.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });

    }
}
