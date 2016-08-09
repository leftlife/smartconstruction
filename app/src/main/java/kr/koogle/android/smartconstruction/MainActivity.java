package kr.koogle.android.smartconstruction;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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

import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.*;
import kr.koogle.android.smartconstruction.util.BackPressCloseHandler;
import kr.koogle.android.smartconstruction.util.RbPreference;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SmartFragment.OnHeadlineSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static BackPressCloseHandler backPressCloseHandler;
    private static RbPreference pref;
    public static FloatingActionButton fab;

    private static ImageView navImage;
    private static TextView navName;
    private static TextView navEmail;
    private static ImageView btnNavSettings;

    // viewPager 관련
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int pageNumber;

    // UTILITY METHODS
    private Toast mToast;
    private Thread mThread;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        if (checkPlayServices()) {
            // GCM 서비스 등록
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "GCM 시작 / token : " + token);
            // 이 token을 서버에 전달 한다.
        }

        // ToolBar 관련
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // viewPager 관련
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // ViewPager 에 TabLayout 연결
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // 네비게이션 이벤트
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset == 0) {
                    // drawer closed
                    invalidateOptionsMenu();
                } else if (slideOffset != 0) {
                    // started opening
                    final TextView navName = (TextView) findViewById(R.id.nav_name);
                    final TextView navEmail = (TextView) findViewById(R.id.nav_email);
                    navName.setText(pref.getValue("pref_user_name", ""));
                    navEmail.setText(pref.getValue("pref_user_email", ""));

                    invalidateOptionsMenu();
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션 연동
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Settings 버튼 클릭 이벤트
        View navHeaderView = navigationView.getHeaderView(0);
        btnNavSettings = (ImageView) navHeaderView.findViewById(R.id.btn_nav_settings);
        btnNavSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnNavSettings.setEnabled(true);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    backPressCloseHandler.onBackPressed();
                    //super.onBackPressed();
                }
            }
        });

        // Floating Action Button 관련
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.img_camera_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(MainActivity.this)
                        .title("촬영선택")
                        .items(new String[]{"사진 촬영", "동영상 촬영"})
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                // showToast(which + ": " + text);
                                switch(which) {
                                    case 0: // 사진 촬영
                                        Intent intent = new Intent(MainActivity.this, CameraPicActivity.class);
                                        startActivity(intent);
                                        break;

                                    case 1: // 동영상 촬영
                                        break;
                                }
                            }
                        })
                        .show();

                // Snackbar.make(view, "사진촬영을 시작합니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        // 삼성런처에서만 가능한 벳지 카운트
        ShortcutBadger.applyCount(this, 10);

        // 닫힐때 한번 더 확인
        backPressCloseHandler = new BackPressCloseHandler(this);

        /******************************************************************************************/
        if(SmartSingleton.arrLaborCategorys.isEmpty()) {
            // Labor Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getLaborCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseLaborCategorys = response.body();

                        if (responseLaborCategorys.size() != 0) {
                            Log.d(TAG, "arrLaborCategorys : size " + responseLaborCategorys.size());
                            SmartSingleton.arrLaborCategorys.addAll(responseLaborCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseLaborCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrMaterialCategorys.isEmpty()) {
            // Material Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getMaterialCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseMaterialCategorys = response.body();

                        if (responseMaterialCategorys.size() != 0) {
                            Log.d(TAG, "arrMaterialCategorys : size " + responseMaterialCategorys.size());
                            SmartSingleton.arrMaterialCategorys.addAll(responseMaterialCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseMaterialCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrEquipmentCategorys.isEmpty()) {
            // Equipment Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getEquipmentCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseEquipmentCategorys = response.body();

                        if (responseEquipmentCategorys.size() != 0) {
                            Log.d(TAG, "arrEquipmentCategorys : size " + responseEquipmentCategorys.size());
                            SmartSingleton.arrEquipmentCategorys.addAll(responseEquipmentCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseEquipmentCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrUnitCategorys.isEmpty()) {
            // Unit Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getUnitCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseUnitCategorys = response.body();

                        if (responseUnitCategorys.size() != 0) {
                            Log.d(TAG, "arrUnitCategorys : size " + responseUnitCategorys.size());
                            SmartSingleton.arrUnitCategorys.addAll(responseUnitCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseUnitCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrWeatherCategorys.isEmpty()) {
            // Weather Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getWeatherCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseWeatherCategorys = response.body();

                        if (responseWeatherCategorys.size() != 0) {
                            Log.d(TAG, "arrWeatherCategorys : size " + responseWeatherCategorys.size());
                            SmartSingleton.arrWeatherCategorys.addAll(responseWeatherCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseWeatherCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrSmartEmployees.isEmpty()) {
            // Equipment Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartEmployee>> call = smartService.getSmartEmployees();

            call.enqueue(new Callback<ArrayList<SmartEmployee>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartEmployee>> call, Response<ArrayList<SmartEmployee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartEmployee> responses = response.body();

                        if (responses.size() != 0) {
                            Log.d(TAG, "arrSmartEmployees : size " + responses.size());
                            SmartSingleton.arrSmartEmployees.addAll(responses);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseEquipmentCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartEmployee>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/

    }

    //  ############## Fragment 통신 ##################  // SmartFragment 용
    public void onArticleSelected(int position) {
        SmartFragment smartFragment = (SmartFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_smart);
        if(smartFragment != null) {
            // Framgment 통신 사용 !!! -> OneFragment
            smartFragment.updateArticleView(position);
        } else {
            /*
            OneFragment newFragment = new OneFragment();
            Bundle args = new Bundle();
            args.putString("strId", "leftlife");
            newFragment.setArguments(args); // Fragment 생성시 데이타 넘길때 꼭 이렇게 !!

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            */
        }
    }

    /*
     * 엑티비티 생명주기 관련 함수 ************************************************************************
     */
    @Override
    protected void onResume() {
        super.onResume();
        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        String redirectUri = "";
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            // use the parameter your API exposes for the code (mostly it's "code")
            String code = "code"; //uri.getQueryParameter("code");
            if (code != null) {

            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
            }
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        SmartSingleton.getInstance().arrSmartBuilds.clear();
        SmartSingleton.getInstance().arrSmartWorks.clear();
        SmartSingleton.getInstance().arrSmartClients.clear();
        SmartSingleton.getInstance().arrSmartOrders.clear();
        SmartSingleton.getInstance().arrSmartPhotos.clear();
        SmartSingleton.getInstance().arrSmartEmployees.clear();

        SmartSingleton.getInstance().arrLaborCategorys.clear();
        SmartSingleton.getInstance().arrMaterialCategorys.clear();
        SmartSingleton.getInstance().arrEquipmentCategorys.clear();

        SmartSingleton.getInstance().arrComments.clear();

        super.onDestroy();
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Log.d(TAG, "getCurrentItem : " + String.valueOf(mViewPager.getCurrentItem()));
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            switch(mViewPager.getCurrentItem()) {

                case 0: // 스마트 현장
                    Intent intent = new Intent(MainActivity.this, SmartWorkViewActivity.class);
                    intent.putExtra("strCode", "");
                    //SmartSingleton.smartClient = new SmartClient();
                    startActivityForResult(intent, 1001);
                    break;

                case 1: // 건축주 협의
                    intent = new Intent(MainActivity.this, SmartClientWriteActivity.class);
                    intent.putExtra("intId", 0);
                    //SmartSingleton.smartClient = new SmartClient();
                    startActivityForResult(intent, 1001);
                    break;

                case 2: // 작업지시
                    intent = new Intent(MainActivity.this, SmartOrderWriteActivity.class);
                    intent.putExtra("intId", 0);
                    //SmartSingleton.smartClient = new SmartClient();
                    startActivityForResult(intent, 1001);
                    break;

                default:
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Navigation 메뉴 관련 함수 ************************************************************************
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_smart_build) {
            Intent intent = new Intent(MainActivity.this, SmartWorkViewActivity.class);
            intent.putExtra("strCode", "");
            //SmartSingleton.smartClient = new SmartClient();
            startActivityForResult(intent, 1001);

        } else if (id == R.id.nav_smart_client) {
            Intent intent = new Intent(MainActivity.this, SmartClientWriteActivity.class);
            intent.putExtra("intId", 0);
            //SmartSingleton.smartClient = new SmartClient();
            startActivityForResult(intent, 1001);

        } else if (id == R.id.nav_smart_order) {
            Intent intent = new Intent(MainActivity.this, SmartOrderWriteActivity.class);
            intent.putExtra("intId", 0);
            //SmartSingleton.smartClient = new SmartClient();
            startActivityForResult(intent, 1001);

        } else if (id == R.id.nav_manage_photo) {
            Intent intent = new Intent(MainActivity.this, CameraPicListActivity.class);
            intent.putExtra("intId", SmartSingleton.smartClient.intId);
            startActivityForResult(intent, 2001);

        } else if (id == R.id.nav_manage_employee) {
            Intent intent = new Intent(MainActivity.this, SmartEmployeeActivity.class);
            intent.putExtra("intId", "");
            startActivityForResult(intent, 3001);

        } else if (id == R.id.nav_logout) {
            // Settings 값 !!
            RbPreference pref = new RbPreference(getApplicationContext());
            pref.put("pref_user_code", "");
            pref.put("pref_user_id", "");
            pref.put("pref_user_name", "");
            pref.put("pref_user_type", "");
            pref.put("pref_user_group", "");
            pref.put("pref_user_phone", "");
            pref.put("pref_user_email", "");
            pref.put("pref_access_token", "");

            SmartSingleton.clearInstance();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(int reqNewFragmentIndex)
    {
        /*
        Fragment newFragment = null;
        newFragment = getFragment(reqNewFragmentIndex);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
        */
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
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "스마트현장";
                case 1:
                    return "건축주협의";
                case 2:
                    return "작업지시";
            }
            return null;
        }

        private Fragment getFragment(int idx) {
            Fragment tempFragment = null;

            switch(idx) {
                case 0:
                    tempFragment = new SmartBuildFragment();
                    break;
                case 1:
                    tempFragment = new SmartClientFragment();
                    break;
                case 2:
                    tempFragment = new SmartOrderFragment();
                    break;
                default:
                    Log.d("getFragment", "Unhandle Case");
                    break;
            }
            return tempFragment;
        }
    }

    /*
     * Util 함수들 ##################################################################
     */
    // GCM 이 가능한지 체크
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

}