package kr.koogle.android.smartconstruction;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.ZoomStyle;
import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.OnClick;
import kr.koogle.android.smartconstruction.http.FileUploadService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_CHECKIN_PROPERTIES;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class CameraPicActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "CameraPicActivity";
    private RbPreference pref;
    private static final String[] PERMS_ALL={
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private static final FlashMode[] FLASH_MODES={
            FlashMode.ALWAYS,
            FlashMode.AUTO
    };
    private static final int REQUEST_PORTRAIT_RFC=1337;
    private static final int REQUEST_PORTRAIT_FFC=REQUEST_PORTRAIT_RFC+1;
    private static final int REQUEST_LANDSCAPE_RFC=REQUEST_PORTRAIT_RFC+2;
    private static final int REQUEST_LANDSCAPE_FFC=REQUEST_PORTRAIT_RFC+3;
    private static final int RESULT_PERMS_ALL=REQUEST_PORTRAIT_RFC+4;
    private static final String STATE_PAGE="cwac_cam2_demo_page";
    private static final String STATE_TEST_ROOT="cwac_cam2_demo_test_root";
    private static final String STATE_IS_VIDEO="cwac_cam2_demo_is_video";
    private static final String STATE_MY_BITMAP="cwac_my_bitmap";

    private File testRoot;
    private File testZip;
    private String testFileName;
    private RuntimePermissionUtils utils;
    private File previewFrame;
    private boolean isVideo=false;

    private TextView txtContent;
    private ImageView imgContent;
    private Bitmap myBitmap;

    private static ImageView imgPicture;
    private String strBuildCode;
    private TextView inputBuildName;
    private TextView inputBuildKind;
    private TextView inputLocation;
    private TextView inputMemo;
    private TextView inputDate;
    private Uri fileURI;

    private LocationManager locationManager = null; // 위치 정보 프로바이더
    private LocationListener locationListener = null; //위치 정보가 업데이트시 동작
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    // UTILITY METHODS
    private Toast mToast;
    private MaterialDialog md;

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // ToolBar 관련
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_camera_pic);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPicActivity.this.finish();
            }
        });

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Cannot access external storage!", Toast.LENGTH_LONG).show();
            finish();
        }

        previewFrame= new File(getExternalCacheDir(), "cam2-preview.jpg");
        //Toast.makeText(this, previewFrame.getAbsolutePath(), Toast.LENGTH_LONG).show();

        // 퍼미션 체크 요청 유틸 !!
        utils = new RuntimePermissionUtils(this);

        if (savedInstanceState==null) { // 처음 앱이 실행될 때 !!
            String filename="cam2_"+ Build.MANUFACTURER+"_"+Build.PRODUCT
                    +"_"+new SimpleDateFormat("yyyyMMdd'-'HHmmss").format(new Date());

            filename=filename.replaceAll(" ", "_");

            testRoot=new File(getExternalFilesDir(null), filename);

            String baseDir = testRoot.getAbsolutePath();
            Toast.makeText(this, baseDir, Toast.LENGTH_LONG).show();
        }
        else {

            //wizardBody.setDisplayedChild(savedInstanceState.getInt(STATE_PAGE, 0));
            testRoot=new File(savedInstanceState.getString(STATE_TEST_ROOT));
            isVideo=savedInstanceState.getBoolean(STATE_IS_VIDEO, false);
            String baseDir = testRoot.getAbsolutePath();
            //Toast.makeText(this, STATE_TEST_ROOT, Toast.LENGTH_LONG).show();

            myBitmap=savedInstanceState.getParcelable(STATE_MY_BITMAP); // myBitmap 값을 받아온다 !!!!!
            try {
                imgContent.setImageBitmap(myBitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        testZip=new File(testRoot.getAbsolutePath()+".zip");

        // 퍼미션 채크해서 퍼미션 요청 !!!
        if (!haveNecessaryPermissions() && utils.useRuntimePermissions()) {
            requestPermissions(PERMS_ALL, RESULT_PERMS_ALL);
        }
        else {
            // handlePage();
        }

        // 위치정보 얻기 !!
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //GPS_PROVIDER: GPS를 통해 위치를 알려줌
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //NETWORK_PROVIDER: WI-FI 네트워크나 통신사의 기지국 정보를 통해 위치를 알려줌
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isGPSEnabled && isNetworkEnabled){
            locationListener = new MyLocationListener();

            //선택된 프로바이더를 사용해 위치정보를 업데이트
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
        }else{
            showToast("GPS가 꺼져 있습니다.");
        }

        // 이미지 클릭시 촬영 시작 !!
        imgPicture = (ImageView) findViewById(R.id.img_picture);
        imgPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i=new CameraActivity.IntentBuilder(getApplication())
                        .skipConfirm()
                        .facing(Facing.BACK)
                        .facingExactMatch()
                        .to(new File(testRoot, "portrait-rear.jpg"))
                        .updateMediaStore()
                        .debug()
                        .debugSavePreviewFrame()
                        .flashModes(FLASH_MODES)
                        .zoomStyle(ZoomStyle.SEEKBAR)
                        .build();

                startActivityForResult(i, REQUEST_PORTRAIT_RFC);
            }
        });

        // 사진 상세내용 저장 !!
        inputBuildName = (TextView) findViewById(R.id.input_build_name);
        inputBuildKind = (TextView) findViewById(R.id.input_build_kind);
        inputLocation = (TextView) findViewById(R.id.input_location);
        inputMemo = (TextView) findViewById(R.id.input_memo);
        inputDate = (TextView) findViewById(R.id.input_date);

        inputBuildName.setInputType(0);
        inputBuildName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ArrayList<String> arrBuild = new ArrayList<String>();
                if ( ! SmartSingleton.arrSmartBuilds.isEmpty() ) {
                    //ArrayList<SmartBuild> arrBuild = SmartSingleton.arrSmartBuilds;
                    for(int i=0; i<SmartSingleton.arrSmartBuilds.size(); i++) {
                        arrBuild.add(SmartSingleton.arrSmartBuilds.get(i).strName);
                    }
                }

                MaterialDialog md = new MaterialDialog.Builder(CameraPicActivity.this)
                        .title("현장선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                strBuildCode = SmartSingleton.arrSmartBuilds.get(which).strCode;
                                inputBuildName.setText(text);
                            }
                        })
                        .positiveText("취소하기").show();

            }
        });
        // 초기값 설정
        strBuildCode = SmartSingleton.arrSmartBuilds.get(0).strCode;
        inputBuildName.setText(SmartSingleton.arrSmartBuilds.get(0).strName);

        inputBuildKind.setInputType(0);
        inputBuildKind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ArrayList<String> arrBuild = new ArrayList<String>();
                if ( ! SmartSingleton.arrLaborCategorys.isEmpty() ) {
                    for(int i=0; i<SmartSingleton.arrLaborCategorys.size(); i++) {
                        //arrBuild.add(SmartSingleton.arrLaborCategorys.get(0).arrCategory.get(i).strName);
                        arrBuild.add(SmartSingleton.arrLaborCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(CameraPicActivity.this)
                        .title("공종선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                inputBuildKind.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();

            }
        });

        inputDate.setInputType(0);
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        CameraPicActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");

            }
        });

    }

    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            if (testRoot.exists()) {
                testRoot.delete();
            }

            if (testZip.exists()) {
                testZip.delete();

                // MediaScanner 킷캣이후 버전부터 사용되는 클래스 !!
                MediaScannerConnection.scanFile(
                        this,
                        new String[]{testZip.getAbsolutePath()},
                        null,
                        null);
            }
        }

        super.onDestroy();
    }

    // 엑티비티가 다시 시작될 때 !!
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putInt(STATE_PAGE, wizardBody.getDisplayedChild());
        outState.putString(STATE_TEST_ROOT, testRoot.getAbsolutePath());
        outState.putBoolean(STATE_IS_VIDEO, isVideo);
        outState.putParcelable(STATE_MY_BITMAP, myBitmap);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (haveNecessaryPermissions()) {
            //handlePage();
        }
        else {
            finish();
        }
    }

    private boolean haveNecessaryPermissions() {
        return(utils.hasPermission(CAMERA) &&
                utils.hasPermission(RECORD_AUDIO) &&
                utils.hasPermission(WRITE_EXTERNAL_STORAGE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            // 프로그래스 실행 !!
            showIndeterminateProgressDialog(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showIndeterminateProgressDialog(boolean horizontal) {

        if(fileURI != null) {
            md = new MaterialDialog.Builder(this)
                    .title("이미지 전송중")
                    .content("이미지 전송중 입니다..")
                    .progress(true, 0)
                    .progressIndeterminateStyle(horizontal)
                    .show();
            Log.d(TAG, "fileURI : " + fileURI.toString());
            uploadFile(new File(testRoot.getPath() + "/" + testFileName )); // 서버에 이미지 업로드 !!
        } else {

            new MaterialDialog.Builder(CameraPicActivity.this)
                    .title("이미지 미등록")
                    .content("이미지를 먼저 등록해 주세요.")
                    .positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch(requestCode) {
            case REQUEST_PORTRAIT_RFC:

                //Toast.makeText(this, data.getDataString(), Toast.LENGTH_LONG).show();
                //String fileName = data.getDataString();

                //txtContent = (TextView) findViewById(R.id.txt_content);
                //txtContent.setText(fileName);

                imgContent = (ImageView) findViewById(R.id.img_picture);
                //imgContent.setImageURI(Uri.fromFile(new File(fileName)));

                fileURI = data.getData();
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileURI);
                    imgContent.setImageBitmap(myBitmap);

                    testFileName = "sc_app.jpg"; // + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg"; // 서버에서 이름 자동변경됨 !!
                    createThumbnail(myBitmap, testRoot.getPath(), testFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imgContent.setImageBitmap(myBitmap);

        }
    }

    // 현재 위치 정보를 받기위해 선택한 프로바이더에 위치 업데이터 요청! requestLocationUpdates()메소드를 사용함.
    private class MyLocationListener implements LocationListener {

        @Override
        //LocationListener을 이용해서 위치정보가 업데이트 되었을때 동작 구현
        public void onLocationChanged(Location loc) {
            // 좌표 정보 얻어 토스트메세지 출력
            // Toast.makeText(getBaseContext(), "Location changed : Lat" + loc.getLatitude() + "Lng: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();

            // 뷰에 출력하기 위해 스트링으로 저장
            SmartSingleton.getInstance().strLng = loc.getLongitude();
            SmartSingleton.getInstance().strLat = loc.getLatitude();

            // 도시명 구하기
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try{
                addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if(addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }catch(IOException e){
                e.printStackTrace();
            }
            String s = "당신의 현재 도시명 : " + cityName;
            // editLocation.setText(s);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
    }

    /*
     * Util 함수들 ##################################################################
     */
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String secondString = second < 10 ? "0"+second : ""+second;
        String time = ""+hourString+"h"+minuteString+"m"+secondString+"s";

        inputDate.setText(time);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String strMonth = (monthOfYear+1) < 10 ? "0"+(monthOfYear+1) : ""+(monthOfYear+1);
        String strDay = dayOfMonth < 10 ? "0"+dayOfMonth : ""+dayOfMonth;
        String date = ""+year+"."+strMonth+"."+strDay;

        inputDate.setText(date);
    }

    public void uploadFile(File fileImage) {
        /******************************************************************************************/
        FileUploadService service = ServiceGenerator.createService(FileUploadService.class);
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        File file = fileImage; //FileUtils.getFile(this, fileUri); // Uri 값을 받을 경우 !!

        // Toast.makeText(getBaseContext(), "name : " + file.getName() + " / size : " + file.length() , Toast.LENGTH_SHORT).show();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        // MultipartBody.Part 에 업로드 할 파일 추가 !!
        MultipartBody.Part body = MultipartBody.Part.createFormData("userfile1", file.getName(), requestFile);

        // multipart request 에 전달할 값 추가 !!
        Map<String, RequestBody> querys = new HashMap<>();
        RequestBody rbSiteId = RequestBody.create(MediaType.parse("multipart/form-data"), pref.getValue("pref_user_group", ""));
        RequestBody rbWriter = RequestBody.create(MediaType.parse("multipart/form-data"), pref.getValue("pref_user_id", ""));
        RequestBody rbBuildCode = RequestBody.create(MediaType.parse("multipart/form-data"), strBuildCode);
        RequestBody rbBuildName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(inputBuildName.getText()));
        RequestBody rbBuildKind = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(inputBuildKind.getText()));
        RequestBody rbLocation = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(inputLocation.getText()));
        RequestBody rbMemo = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(inputMemo.getText()));
        RequestBody rbDate = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(inputDate.getText()));
        RequestBody rbLng = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(SmartSingleton.getInstance().strLng));
        RequestBody rbLat = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(SmartSingleton.getInstance().strLat));
        querys.put("strSiteId", rbSiteId);
        querys.put("strWriter", rbWriter);
        querys.put("strBuildCode", rbBuildCode);
        querys.put("strBuildName", rbBuildName);
        querys.put("strLavorCode", rbBuildKind);
        querys.put("strLocation", rbLocation);
        querys.put("strMemo", rbMemo);
        querys.put("strBuildDate", rbDate);
        querys.put("strLng", rbLng);
        querys.put("strLat", rbLat);

        Call<ResponseBody> call = service.upload(querys, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.v("Upload", "success / " + response.body().string());
                    md.hide();

                    new MaterialDialog.Builder(CameraPicActivity.this)
                            .title("사진 업로드 완료")
                            .content("사진이 정상적으로 업로드 되었습니다.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    md.dismiss();
                                    CameraPicActivity.this.finish();
                                }
                            })
                            .show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }

    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }

    public Uri getUriFromPath(String path) {
        String fileName= path; // "file:///sdcard/DCIM/Camera/2013_07_07_12345.jpg";
        Uri fileUri = Uri.parse( fileName );
        String filePath = fileUri.getPath();
        Cursor cursor = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_data = '" + filePath + "'", null, null );
        cursor.moveToNext();
        int id = cursor.getInt( cursor.getColumnIndex( "_id" ) );
        Uri uri = ContentUris.withAppendedId( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id );

        return uri;
    }

    private void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    //bitmap에는 비트맵, strFilePath에는 파일을 저장할 경로, filename 에는 파일 이름을 할당
    public void createThumbnail(Bitmap bitmap, String strFilePath, String filename) {
        File file = new File(strFilePath);
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        File fileCacheItem = new File(strFilePath + "/" + filename);
        OutputStream out = null;

        try {
            int height=bitmap.getHeight();
            int width=bitmap.getWidth();

            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            //160 부분을 자신이 원하는 크기로 변경할 수 있습니다.
            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, height/(width/1000), true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}