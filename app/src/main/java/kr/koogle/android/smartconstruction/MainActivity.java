package kr.koogle.android.smartconstruction;

import android.os.Bundle;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import kr.koogle.android.smartconstruction.http.*;
import kr.koogle.android.smartconstruction.util.BackPressCloseHandler;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BackPressCloseHandler backPressCloseHandler;

    // viewPager 관련
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    // you should either define client id and secret as constants or in string resources
    private final String clientId = "your-client-id";
    private final String clientSecret = "your-client-secret";
    private final String redirectUri = "your://redirecturi";
    static final int REQ_CODE_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기본값 저장
        SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
        String spGCMToken = setting.getString("GCMToken", "");
        String spAuthToken = setting.getString("authToken", "");
        String spPushSmart = setting.getString("pushSmart", "");
        String spPushWork = setting.getString("pushWork", "");
        String spPushBBS = setting.getString("pushBBS", "");
        // Toast.makeText(getBaseContext(), "spAuthToken : "+spAuthToken, Toast.LENGTH_SHORT).show();

        /*
        Intent intentLoading = new Intent(this, LoadingActivity.class);
        startActivity(intentLoading);
        */

        if (spAuthToken.equals("")) // AuthToken 값이 없으면 로그인 Activity 이동
        {
            Log.d(TAG, "로그인 창 열림!!");
            Intent intentLogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentLogin);
            Log.d(TAG, "로그인 창 닫힘!!");
        }
        backPressCloseHandler = new BackPressCloseHandler(this);

        if (checkPlayServices()) {
            // GCM 서비스 등록
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "GCM 시작 / token : " + token);
            // 이 token을 서버에 전달 한다.
        }


        // Tool Bar 관련
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // viewPager 관련
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // TabLayout 관련
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        /*
        tabLayout.addTab(tabLayout.newTab().setText("스마트일보"));
        tabLayout.addTab(tabLayout.newTab().setText("작업지시"));
        tabLayout.addTab(tabLayout.newTab().setText("공지사항"));
        */
        tabLayout.setupWithViewPager(mViewPager);

        // Floating Action Button 관련
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        // 삼성런처에서만 가능한 벳지 카운트
        int badgeCount = 12;
        Intent intentBadge = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intentBadge.putExtra("badge_count", badgeCount);
        // 메인 메뉴에 나타나는 어플의  패키지 명
        intentBadge.putExtra("badge_count_package_name", getComponentName().getPackageName());
        // 메인메뉴에 나타나는 어플의 클래스 명
        intentBadge.putExtra("badge_count_class_name", getComponentName().getClassName());
        sendBroadcast(intentBadge);

        /*
        // picasso 사용예
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        Picasso.with(this)
                .load("https://tpc.googlesyndication.com/simgad/11582263531426008338")
                .placeholder(R.drawable.common_full_open_on_phone)
                .error(R.drawable.common_plus_signin_btn_icon_dark)
                .resize(100,100) // fit() 알아서 용량 줄여서 가져옴.  // centerCrop()
                .rotate(90)
                .into(imageView);
        */
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }



    // 실행중인 앱인지 체크하기
    public static boolean isRunningProcess(Context context, String packageName) {

        boolean isRunning = false;
        ActivityManager actMng = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if (rap.processName.equals(packageName))
            {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    // 자식 엑티비티에서 결과값 받아 처리하기
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode, Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        // Uri에서 이미지 이름을 얻어온다.
                        String name_Str = getImageNameToUri(data.getData());

                        // 이미지 데이터를 비트맵으로 받아온다.
                        Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        ImageView image = (ImageView)findViewById(R.id.imageView1);

                        // 배치해놓은 ImageView에 set
                        image.setImageBitmap(image_bitmap);


                        final Uri selectImageUri = data.getData();
                        final String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        final Cursor imageCursor = this.getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
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
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }

    private void uploadFile(File fileImage) {
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

    // 엑티비티가 시작될 때 수행할 작업
    @Override
    protected void onResume() {
        super.onResume();

        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(redirectUri) || true) {
            // use the parameter your API exposes for the code (mostly it's "code")
            String code = "code"; //uri.getQueryParameter("code");
            if (code != null) {

                /*
                // get access token
                Log.d(TAG, "loginService.getAccessToken 실행!!");
                LoginService loginService =
                        ServiceGenerator.createService(LoginService.class, clientId, clientSecret);
                Call<AccessToken> call = loginService.getAccessToken(code, "authorization_code");

                call.enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                        //get raw response
                        okhttp3.Response raw = response.raw();

                        if (response.isSuccessful()) {
                            // tasks available
                            Toast.makeText(getBaseContext(), "success : " + response.toString() , Toast.LENGTH_SHORT).show();
                        } else {
                            // error response, no access to resource?
                            Toast.makeText(getBaseContext(), "failure : " + response.toString() , Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {
                        // something went completely south (like no internet connection)
                        Toast.makeText(getBaseContext(), "error : " + t.getMessage() , Toast.LENGTH_SHORT).show();
                        Log.d("Error", t.getMessage());
                    }
                });
                */

                /*
                try {
                    AccessToken accessToken = call.execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */

                /*
                // get access token (custom)
                LoginService loginService =
                        ServiceGenerator.createService(LoginService.class, "user", "secretpassword");
                Call<User> call = loginService.basicLogin();

                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        //get raw response
                        okhttp3.Response raw = response.raw();

                        if (response.isSuccessful()) {
                            // tasks available
                            Toast.makeText(getBaseContext(), "success : " + response.toString() , Toast.LENGTH_SHORT).show();
                        } else {
                            // error response, no access to resource?
                            Toast.makeText(getBaseContext(), "failure : " + response.toString() , Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        // something went completely south (like no internet connection)
                        Toast.makeText(getBaseContext(), "error : " + t.getMessage() , Toast.LENGTH_SHORT).show();
                        Log.d("Error", t.getMessage());
                    }
                });
                */

            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
            }
        }
    }



    // Drawer Layout 관련
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backPressCloseHandler.onBackPressed();
            //super.onBackPressed();
        }
    }

    /*
     * App Bar 메뉴 관련 함수 ************************************************************************
     */
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*
     * viewPager 관련 Adapter 클래스 ##################################################################
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            return getFragment(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "스마트일보";
                case 1:
                    return "작업지시";
                case 2:
                    return "공지사항";
            }
            return null;
        }

        private Fragment getFragment(int idx) {
            Fragment tempFragment = null;

            switch(idx) {
                case 0:
                    tempFragment = new OneFragment();
                    break;
                case 1:
                    tempFragment = new TwoFragment();
                    break;
                case 2:
                    tempFragment = new OneFragment();
                    break;
                default:
                    Log.d("getFragment", "Unhandle Case");
                    break;
            }
            return tempFragment;
        }
    }

}