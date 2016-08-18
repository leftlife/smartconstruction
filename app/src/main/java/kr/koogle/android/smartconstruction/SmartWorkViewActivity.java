package kr.koogle.android.smartconstruction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartCategory;
import kr.koogle.android.smartconstruction.http.SmartEmployee;
import kr.koogle.android.smartconstruction.http.SmartEquipment;
import kr.koogle.android.smartconstruction.http.SmartLabor;
import kr.koogle.android.smartconstruction.http.SmartMaterial;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetterLee;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartWorkViewActivity extends AppCompatActivity {
    private static final String TAG = "SmartWorkViewActivity";
    private RbPreference pref;

    @Bind(R.id.txt_work_view_build_name) TextView _txtBuildName;
    @Bind(R.id.txt_work_view_date) TextView _txtDate;
    @Bind(R.id.txt_work_view_weather) TextView _txtWeather;
    @Bind(R.id.txt_work_view_memo) TextView _txtMemo;

    @Bind(R.id.btn_work_view_delete) ImageView _btnDelete;
    @Bind(R.id.btn_work_view_add_labor) ImageView _btnAddLabor;
    @Bind(R.id.btn_work_view_add_material) ImageView _btnAddMaterial;
    @Bind(R.id.btn_work_view_add_equipment) ImageView _btnAddEquipment;
    @Bind(R.id.btn_work_view_add_photo) ImageView _btnAddPhoto;

    // row_work_view_labor
    @Bind(R.id.ll_work_view_add_labor) LinearLayout _llWorkViewAddLabor;
    @Bind(R.id.txt_work_view_labor_cate1) TextView _txtWorkViewLaborCate1;
    @Bind(R.id.txt_work_view_labor_cate2) TextView _txtWorkViewLaborCate2;
    @Bind(R.id.txt_work_view_labor_count) EditText _txtWorkViewLaborCount;
    @Bind(R.id.txt_work_view_labor_unit) TextView _txtWorkViewLaborUnit;
    @Bind(R.id.txt_work_view_labor_memo) EditText _txtWorkViewLaborMemo;
    @Bind(R.id.btn_work_view_labor_add) Button _btnWorkViewLaborAdd;

    // row_work_view_material
    @Bind(R.id.ll_work_view_add_material) LinearLayout _llWorkViewAddMaterial;
    @Bind(R.id.txt_work_view_material_cate1) TextView _txtWorkViewMaterialCate1;
    @Bind(R.id.txt_work_view_material_cate2) TextView _txtWorkViewMaterialCate2;
    @Bind(R.id.txt_work_view_material_count) EditText _txtWorkViewMaterialCount;
    @Bind(R.id.txt_work_view_material_unit) TextView _txtWorkViewMaterialUnit;
    @Bind(R.id.txt_work_view_material_memo) EditText _txtWorkViewMaterialMemo;
    @Bind(R.id.btn_work_view_material_add) Button _btnWorkViewMaterialAdd;

    // row_work_view_equipment
    @Bind(R.id.ll_work_view_add_equipment) LinearLayout _llWorkViewAddEquipment;
    @Bind(R.id.txt_work_view_equipment_cate1) TextView _txtWorkViewEquipmentCate1;
    @Bind(R.id.txt_work_view_equipment_cate2) TextView _txtWorkViewEquipmentCate2;
    @Bind(R.id.txt_work_view_equipment_count) EditText _txtWorkViewEquipmentCount;
    @Bind(R.id.txt_work_view_equipment_unit) TextView _txtWorkViewEquipmentUnit;
    @Bind(R.id.txt_work_view_equipment_memo) EditText _txtWorkViewEquipmentMemo;
    @Bind(R.id.btn_work_view_equipment_add) Button _btnWorkViewEquipmentAdd;

    public static RecyclerView recyclerViewLabor;
    private SmartWorkLaborAdapter adapterLabor;
    private RecyclerView.LayoutManager layoutManagerLabor;

    public static RecyclerView recyclerViewMaterial;
    private SmartWorkMaterialAdapter adapterMaterial;
    private RecyclerView.LayoutManager layoutManagerMaterial;

    public static RecyclerView recyclerViewEquipment;
    private SmartWorkEquipmentAdapter adapterEquipment;
    private RecyclerView.LayoutManager layoutManagerEquipment;

    public static RecyclerView recyclerViewPhoto;
    private SmartWorkPhotoAdapter adapterPhoto;
    private RecyclerView.LayoutManager layoutManagerPhoto;

    private ScrollView scrollView;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String strBuildCode;
    private String strWorkCode;

    // UTILITY METHODS
    private Toast mToast;
    private MaterialDialog md;
    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_view);
        ButterKnife.bind(this);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);
        // intent 등록
        intent = getIntent();
        // 리스트 클릭시 넘어온값 받기 !!
        strBuildCode = getIntent().getExtras().getString("strBuildCode");
        strWorkCode = getIntent().getExtras().getString("strCode");

        // RecyclerView 저장 (노무) ------------------------------------------------------------------
        recyclerViewLabor = (RecyclerView) findViewById(R.id.rv_work_view_labors);
        // LayoutManager 저장
        layoutManagerLabor = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewLabor.setLayoutManager(layoutManagerLabor);
        /******************************************************************************************/
        // Adapter 생성
        adapterLabor = new SmartWorkLaborAdapter(this, SmartSingleton.smartWork.arrSmartLabors);
        // RecycleView 에 Adapter 세팅
        recyclerViewLabor.setAdapter(adapterLabor);
        // 리스트 표현하기 !!
        recyclerViewLabor.setItemAnimator(new SlideInUpAnimator());
        /***************************************************************************/
        adapterLabor.setOnItemClickListener(new SmartWorkLaborAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartLabors.get(position).intId);
                adapterLabor.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // RecyclerView 저장 (자재) ------------------------------------------------------------------
        recyclerViewMaterial = (RecyclerView) findViewById(R.id.rv_work_view_materials);
        // LayoutManager 저장
        layoutManagerMaterial = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewMaterial.setLayoutManager(layoutManagerMaterial);
        /******************************************************************************************/
        // Adapter 생성
        adapterMaterial = new SmartWorkMaterialAdapter(this, SmartSingleton.smartWork.arrSmartMaterials);
        // RecycleView 에 Adapter 세팅
        recyclerViewMaterial.setAdapter(adapterMaterial);
        // 리스트 표현하기 !!
        recyclerViewMaterial.setItemAnimator(new SlideInUpAnimator());
        /***************************************************************************/
        adapterMaterial.setOnItemClickListener(new SmartWorkMaterialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartMaterials.get(position).intId);
                adapterMaterial.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // RecyclerView 저장 (장비) ------------------------------------------------------------------
        recyclerViewEquipment = (RecyclerView) findViewById(R.id.rv_work_view_equipments);
        // LayoutManager 저장
        layoutManagerEquipment = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewEquipment.setLayoutManager(layoutManagerEquipment);
        /******************************************************************************************/
        // Adapter 생성
        adapterEquipment = new SmartWorkEquipmentAdapter(this, SmartSingleton.smartWork.arrSmartEquipments);
        // RecycleView 에 Adapter 세팅
        recyclerViewEquipment.setAdapter(adapterEquipment);
        // 리스트 표현하기 !!
        recyclerViewEquipment.setItemAnimator(new SlideInUpAnimator());
        /***************************************************************************/
        adapterEquipment.setOnItemClickListener(new SmartWorkEquipmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartEquipments.get(position).intId);
                adapterEquipment.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // RecyclerView 저장 (작업사진) ------------------------------------------------------------------
        recyclerViewPhoto = (RecyclerView) findViewById(R.id.rv_work_view_photos);
        // LayoutManager 저장
        layoutManagerPhoto = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewPhoto.setLayoutManager(layoutManagerPhoto);
        /******************************************************************************************/
        // Adapter 생성
        adapterPhoto = new SmartWorkPhotoAdapter(this, SmartSingleton.smartWork.arrSmartPhotos);
        // RecycleView 에 Adapter 세팅
        recyclerViewPhoto.setAdapter(adapterPhoto);
        // 리스트 표현하기 !!
        recyclerViewPhoto.setItemAnimator(new SlideInUpAnimator());
        /***************************************************************************/
        adapterPhoto.setOnItemClickListener(new SmartWorkPhotoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartPhotos.get(position).intId);
                adapterPhoto.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // 내용 넣는 부분
        SmartSingleton.smartWork.reset();
        if( !strWorkCode.equals("") ) { // 기존 내용 보기이면..

            wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
            wheel.setVisibility(View.VISIBLE);
            wheel.setBarColor(R.color.colorPrimary);
            wheel.spin();

            writeWork();
        }

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartWorkViewActivity.this.finish();
            }
        });

        // 스마트 일보 삭제
        _btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("스마트일보 삭제")
                        .content("삭제된 데이터는 복구되지 않습니다. 정말로 삭제하시겠습니까?")
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                deleteWork(SmartSingleton.smartWork.strCode);
                            }
                        })
                        .show();
            }
        });

        // 공사명 선택
        _txtBuildName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrSmartBuilds.isEmpty()) {
                    //ArrayList<SmartBuild> arrBuild = SmartSingleton.arrSmartBuilds;
                    for (int i = 0; i < SmartSingleton.arrSmartBuilds.size(); i++) {
                        arrBuild.add(SmartSingleton.arrSmartBuilds.get(i).strName);
                    }
                }

                MaterialDialog md = new MaterialDialog.Builder(SmartWorkViewActivity.this)
                    .title("현장명 선택")
                    .items(arrBuild)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            strBuildCode = SmartSingleton.arrSmartBuilds.get(which).strCode;
                            SmartSingleton.smartWork.strBuildCode = SmartSingleton.arrSmartBuilds.get(which).strCode;
                            _txtBuildName.setText(text);
                            _txtBuildName.clearFocus();
                        }
                    })
                    .positiveText("창닫기").show();
            }
        });

        // 작업일 선택
        _txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
                        //Your code.
                        String strMonth = (monthOfYear+1) < 10 ? "0"+(monthOfYear+1) : ""+(monthOfYear+1);
                        String strDay = dayOfMonth < 10 ? "0"+dayOfMonth : ""+dayOfMonth;
                        String date = ""+year+"."+strMonth+"."+strDay;

                        SmartSingleton.smartWork.strDate = date;
                        _txtDate.setText(date);
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                dpd.show(getFragmentManager(), "Datepickerdialog");
                _txtDate.clearFocus();
            }
        });

        // 날씨 선택
        _txtWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrWeatherCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrWeatherCategorys.size(); i++) {
                        arrBuild.add(SmartSingleton.arrWeatherCategorys.get(i).strName);
                    }
                }

                MaterialDialog md = new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("현장 날씨 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SmartSingleton.smartWork.intWeather = which;
                                _txtWeather.setText(text);
                                _txtWeather.clearFocus();
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });

        // 특기사항 등록
        _txtMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("특기사항")
                        //.content("특기사항을 입력하세요.")
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                        .inputRange(2, 1000)
                        .positiveText("작성완료")
                        .input(_txtMemo.getText().toString(), _txtMemo.getText().toString(), false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                SmartSingleton.smartWork.strMemo = input.toString();
                                _txtMemo.setText(input);
                            }
                        }).show();

            }
        });

        // 금일작업사항 등록 부분 노출
        _btnAddLabor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_llWorkViewAddLabor.getVisibility() == View.VISIBLE) {
                    _llWorkViewAddLabor.setVisibility(View.GONE);
                } else {
                    _llWorkViewAddLabor.setVisibility(View.VISIBLE);
                }
            }
        });
        // 자재현황 등록 부분 노출
        _btnAddMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_llWorkViewAddMaterial.getVisibility() == View.VISIBLE) {
                    _llWorkViewAddMaterial.setVisibility(View.GONE);
                } else {
                    _llWorkViewAddMaterial.setVisibility(View.VISIBLE);
                }
            }
        });
        // 장비현황 등록 부분 노출
        _btnAddEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_llWorkViewAddEquipment.getVisibility() == View.VISIBLE) {
                    _llWorkViewAddEquipment.setVisibility(View.GONE);
                } else {
                    _llWorkViewAddEquipment.setVisibility(View.VISIBLE);
                }
            }
        });

        /***************************************************************************/
        // 작업사항 카테고리1 등록
        _txtWorkViewLaborCate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrLaborCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrLaborCategorys.size(); i++) {
                        //arrBuild.add(SmartSingleton.arrLaborCategorys.get(0).arrCategory.get(i).strName);
                        arrBuild.add(SmartSingleton.arrLaborCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("1차 공종 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewLaborCate1.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 작업사항 카테고리2 등록
        _txtWorkViewLaborCate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_txtWorkViewLaborCate1.getText().equals("")) {
                    Toast.makeText(getApplication(), "1차 카테고리를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrLaborCategorys.isEmpty()) {
                    final String laborCategory1 = _txtWorkViewLaborCate1.getText().toString();
                    final ArrayList<SmartCategory> arrSCs = new ArrayList<SmartCategory>();
                    for (SmartCategory sc2 : SmartSingleton.arrLaborCategorys) {
                        if (sc2.strName.equals(laborCategory1)) {
                            arrSCs.addAll(sc2.arrCategory);
                        }
                    }

                    for (int i = 0; i < arrSCs.size(); i++) {
                        arrBuild.add(arrSCs.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("2차 공종 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewLaborCate2.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 작업사항 단위 등록
        _txtWorkViewLaborUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrUnitCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrUnitCategorys.size(); i++) {
                        arrBuild.add(SmartSingleton.arrUnitCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("단위 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewLaborUnit.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 작업현황 등록
        _btnWorkViewLaborAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLabor(view);
            }
        });

        /***************************************************************************/
        // 자재현황 카테고리1 등록
        _txtWorkViewMaterialCate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if ( !SmartSingleton.arrMaterialCategorys.isEmpty() ) {
                    for (int i = 0; i < SmartSingleton.arrMaterialCategorys.size(); i++) {
                        //arrBuild.add(SmartSingleton.arrMaterialCategorys.get(0).arrCategory.get(i).strName);
                        arrBuild.add(SmartSingleton.arrMaterialCategorys.get(i).strName);
                    }
                }
                Log.d("abcd", "size : " + SmartSingleton.arrMaterialCategorys.size() + "------------------------------------------------------");

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("1차 카테고리 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewMaterialCate1.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 자재현황 카테고리2 등록
        _txtWorkViewMaterialCate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_txtWorkViewMaterialCate1.getText().equals("")) {
                    Toast.makeText(getApplication(), "1차 카테고리를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrMaterialCategorys.isEmpty()) {
                    final String laborCategory1 = _txtWorkViewMaterialCate1.getText().toString();
                    final ArrayList<SmartCategory> arrSCs = new ArrayList<SmartCategory>();
                    for (SmartCategory sc2 : SmartSingleton.arrMaterialCategorys) {
                        if (sc2.strName.equals(laborCategory1)) {
                            arrSCs.addAll(sc2.arrCategory);
                        }
                    }

                    for (int i = 0; i < arrSCs.size(); i++) {
                        arrBuild.add(arrSCs.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("2차 카테고리 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewMaterialCate2.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 자재현황 단위 등록
        _txtWorkViewMaterialUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrUnitCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrUnitCategorys.size(); i++) {
                        arrBuild.add(SmartSingleton.arrUnitCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("단위 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewMaterialUnit.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 자재현황 등록
        _btnWorkViewMaterialAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMaterial(view);
            }
        });

        /***************************************************************************/
        // 장비현황 카테고리1 등록
        _txtWorkViewEquipmentCate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrEquipmentCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrEquipmentCategorys.size(); i++) {
                        //arrBuild.add(SmartSingleton.arrEquipmentCategorys.get(0).arrCategory.get(i).strName);
                        arrBuild.add(SmartSingleton.arrEquipmentCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("1차 카테고리 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewEquipmentCate1.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 장비현황 카테고리2 등록
        _txtWorkViewEquipmentCate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_txtWorkViewEquipmentCate1.getText().equals("")) {
                    Toast.makeText(getApplication(), "1차 카테고리를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrEquipmentCategorys.isEmpty()) {
                    final String laborCategory1 = _txtWorkViewEquipmentCate1.getText().toString();
                    final ArrayList<SmartCategory> arrSCs = new ArrayList<SmartCategory>();
                    for (SmartCategory sc2 : SmartSingleton.arrEquipmentCategorys) {
                        if (sc2.strName.equals(laborCategory1)) {
                            arrSCs.addAll(sc2.arrCategory);
                        }
                    }

                    for (int i = 0; i < arrSCs.size(); i++) {
                        arrBuild.add(arrSCs.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("2차 카테고리 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewEquipmentCate2.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 장비현황 단위 등록
        _txtWorkViewEquipmentUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> arrBuild = new ArrayList<String>();
                if (!SmartSingleton.arrUnitCategorys.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrUnitCategorys.size(); i++) {
                        arrBuild.add(SmartSingleton.arrUnitCategorys.get(i).strName);
                    }
                }

                new MaterialDialog.Builder(SmartWorkViewActivity.this)
                        .title("단위 선택")
                        .items(arrBuild)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                _txtWorkViewEquipmentUnit.setText(text);
                            }
                        })
                        .positiveText("창닫기").show();
            }
        });
        // 장비현황 등록
        _btnWorkViewEquipmentAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEquipment(view);
            }
        });

        /***************************************************************************/
        // 작업사진 등록
        _btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartWorkViewActivity.this, CameraPicListActivity.class);
                intent.putExtra("intId", SmartSingleton.smartOrder.intId);
                startActivityForResult(intent, 2001);
            }
        });

        /***************************************************************************/
        // 리스트 클릭시 상세 페이지 보기
        adapterLabor.setOnItemClickListener(new SmartWorkLaborAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapterLabor.notifyItemChanged(position);
                //Intent intext = new Intent(SmartWorkViewActivity.this, SmartOrderViewActivity.class);
                //final int intId = SmartSingleton.arrComments.get(position).intId;
                //intext.putExtra("intId", intId);
                //startActivityForResult(intext, 1002);
                //Toast.makeText(SmartOrderViewActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
            }
        });

        // 작업사항 리스트 X버튼 클릭시 해당 글 삭제
        adapterLabor.setOnItemXClickListener(new SmartWorkLaborAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {
                adapterLabor.remove(position);
                //Toast.makeText(SmartOrderViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        // 자재현황 리스트 X버튼 클릭시 해당 글 삭제
        adapterMaterial.setOnItemXClickListener(new SmartWorkMaterialAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {
                adapterMaterial.remove(position);
                //Toast.makeText(SmartOrderViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        // 장비현황 리스트 X버튼 클릭시 해당 글 삭제
        adapterEquipment.setOnItemXClickListener(new SmartWorkEquipmentAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {
                adapterEquipment.remove(position);
                //Toast.makeText(SmartOrderViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });

        // 작업사진 리스트 X버튼 클릭시 해당 글 삭제
        adapterPhoto.setOnItemXClickListener(new SmartWorkPhotoAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {
                adapterPhoto.remove(position);
                //Toast.makeText(SmartOrderViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/
    }

    private void writeWork() {
        /******************************************************************************************/
        // SmartBuild 값 불러오기 (진행중인 현장)
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));

        //final Map<String, String> mapOptions = new HashMap<String, String>();
        //mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));
        Call<SmartWork> call = smartService.getSmartWork(strBuildCode, strWorkCode);

        call.enqueue(new Callback<SmartWork>() {
            @Override
            public void onResponse(Call<SmartWork> call, Response<SmartWork> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final SmartWork responses = response.body();

                    if( !responses.strCode.equals("") ) {
                        //Log.d(TAG, "responses : strCode " + responses.strCode);
                        for (SmartBuild sb : SmartSingleton.arrSmartBuilds) {
                            if (sb.strCode.equals(responses.strBuildCode)) {
                                _txtBuildName.setText(sb.strName);
                            }
                        }
                        _txtDate.setText(responses.strDate);
                        for (SmartCategory sc : SmartSingleton.arrWeatherCategorys) {
                            if (sc.strCode.equals(String.valueOf(responses.intWeather))) {
                                _txtWeather.setText(sc.strName);
                            }
                            Log.d("aaaa", "strCode : " + sc.strCode + " / intWeather : " + responses.intWeather);
                        }
                        _txtMemo.setText(responses.strMemo);

                        SmartSingleton.smartWork.strCode = responses.strCode;
                        SmartSingleton.smartWork.strBuildCode = responses.strBuildCode;
                        SmartSingleton.smartWork.strDate = responses.strDate;
                        SmartSingleton.smartWork.intLevel = responses.intLevel;
                        SmartSingleton.smartWork.strMemo = responses.strMemo;
                        SmartSingleton.smartWork.intWeather = responses.intWeather;
                        SmartSingleton.smartWork.strImageURL = responses.strImageURL;
                        SmartSingleton.smartWork.strId = responses.strId;

                        SmartSingleton.smartWork.arrSmartLabors.addAll(responses.arrSmartLabors);
                        int curSizeLabor = adapterLabor.getItemCount();
                        adapterLabor.notifyItemRangeInserted(curSizeLabor, responses.arrSmartLabors.size());

                        SmartSingleton.smartWork.arrSmartMaterials.addAll(responses.arrSmartMaterials);
                        int curSizeMaterial = adapterMaterial.getItemCount();
                        adapterMaterial.notifyItemRangeInserted(curSizeMaterial, responses.arrSmartMaterials.size());

                        SmartSingleton.smartWork.arrSmartEquipments.addAll(responses.arrSmartEquipments);
                        int curSizeEquipment = adapterEquipment.getItemCount();
                        adapterEquipment.notifyItemRangeInserted(curSizeEquipment, responses.arrSmartEquipments.size());

                        SmartSingleton.smartWork.arrSmartPhotos.addAll(responses.arrSmartPhotos);
                        int curSizePhoto = adapterPhoto.getItemCount();
                        adapterPhoto.notifyItemRangeInserted(curSizePhoto, responses.arrSmartPhotos.size());

                        // 쓰기 권한 체크
                        if(SmartSingleton.smartWork.strId.equals(pref.getValue("pref_user_id",""))) {
                            _btnDelete.setVisibility(View.VISIBLE);
                            _btnAddLabor.setVisibility(View.VISIBLE);
                            _btnAddMaterial.setVisibility(View.VISIBLE);
                            _btnAddEquipment.setVisibility(View.VISIBLE);
                            _btnAddPhoto.setVisibility(View.VISIBLE);
                        } else {
                            _btnDelete.setVisibility(View.GONE);
                            _btnAddLabor.setVisibility(View.GONE);
                            _btnAddMaterial.setVisibility(View.GONE);
                            _btnAddEquipment.setVisibility(View.GONE);
                            _btnAddPhoto.setVisibility(View.GONE);
                        }
                    } else {
                        Snackbar.make(SmartWorkActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responses : 데이터가 정확하지 않습니다.");
                }

                wheel.stopSpinning();
                wheel.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SmartWork> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                wheel.stopSpinning();
                wheel.setVisibility(View.GONE);
            }
        });
        /******************************************************************************************/
    }

    private void deleteWork(String strCode) {
        if(strCode.equals("")) {
            this.finish();
            return;
        }
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        //final Map<String, String> mapOptions = new HashMap<String, String>();
        //mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));
        Call<ResponseBody> call = smartService.deleteWork(strBuildCode, strWorkCode);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //smartClient = response.body();
                    Log.d(TAG, response.body().toString());
                    //Log.d(TAG, "title : " + smartClient.arrComments.get(0).strContent.toString());
                    new MaterialDialog.Builder(SmartWorkViewActivity.this)
                            .title("스마트일보 삭제 완료")
                            .content("데이터가 정상적으로 삭제 되었습니다.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    SmartWorkViewActivity.this.finish();
                                }
                            })
                            .show();
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responses : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    private void addLabor(View view){
        if( _txtWorkViewLaborCate1.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("1차 공종을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewLaborCate2.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("2차 공종을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewLaborCount.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("수량을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewLaborUnit.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("단위 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewLaborMemo.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("내용을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }

        final SmartLabor smartLabor = new SmartLabor();
        smartLabor.intId = 0;
        smartLabor.strCate1 = _txtWorkViewLaborCate1.getText().toString();
        smartLabor.strCate2 = _txtWorkViewLaborCate2.getText().toString();
        smartLabor.intCount = Integer.valueOf(_txtWorkViewLaborCount.getText().toString());
        smartLabor.strUnit = _txtWorkViewLaborUnit.getText().toString();
        smartLabor.strMemo = _txtWorkViewLaborMemo.getText().toString();

        SmartSingleton.smartWork.arrSmartLabors.add(smartLabor);
        adapterLabor.notifyItemRangeInserted(adapterLabor.getItemCount(), 1);

        _txtWorkViewLaborCate1.setText("");
        _txtWorkViewLaborCate2.setText("");
        _txtWorkViewLaborCount.setText("");
        _txtWorkViewLaborUnit.setText("");
        _txtWorkViewLaborMemo.setText("");

        _txtWorkViewLaborMemo.requestFocus();
        downKeyboard(this, _txtWorkViewLaborMemo);
    }

    private void addMaterial(View view){
        if( _txtWorkViewMaterialCate1.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("1차 카테고리를 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewMaterialCate2.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("2차 카테고리를 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewMaterialCount.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("수량을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewMaterialUnit.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("단위 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewMaterialMemo.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("내용을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }

        final SmartMaterial smartMaterial = new SmartMaterial();
        smartMaterial.intId = 0;
        smartMaterial.strCate1 = _txtWorkViewMaterialCate1.getText().toString();
        smartMaterial.strCate2 = _txtWorkViewMaterialCate2.getText().toString();
        smartMaterial.intCount = Integer.valueOf(_txtWorkViewMaterialCount.getText().toString());
        smartMaterial.strUnit = _txtWorkViewMaterialUnit.getText().toString();
        smartMaterial.strMemo = _txtWorkViewMaterialMemo.getText().toString();

        SmartSingleton.smartWork.arrSmartMaterials.add(smartMaterial);
        adapterMaterial.notifyItemRangeInserted(adapterMaterial.getItemCount(), 1);

        _txtWorkViewMaterialCate1.setText("");
        _txtWorkViewMaterialCate2.setText("");
        _txtWorkViewMaterialCount.setText("");
        _txtWorkViewMaterialUnit.setText("");
        _txtWorkViewMaterialMemo.setText("");

        _txtWorkViewMaterialMemo.requestFocus();
        downKeyboard(this, _txtWorkViewMaterialMemo);
    }

    private void addEquipment(View view){
        if( _txtWorkViewEquipmentCate1.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("1차 카테고리를 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        /*
        if( _txtWorkViewEquipmentCate2.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("2차 카테고리를 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        */
        if( _txtWorkViewEquipmentCount.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("수량을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewEquipmentUnit.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("단위 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }
        if( _txtWorkViewEquipmentMemo.getText().toString().trim().equals("") ) {
            new MaterialDialog.Builder(SmartWorkViewActivity.this).content("내용을 정확하게 입력해 주세요.").positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    }).show();
            return;
        }

        final SmartEquipment smartEquipment = new SmartEquipment();
        smartEquipment.intId = 0;
        smartEquipment.strCate1 = _txtWorkViewEquipmentCate1.getText().toString();
        smartEquipment.strCate2 = _txtWorkViewEquipmentCate2.getText().toString();
        smartEquipment.intCount = Integer.valueOf(_txtWorkViewEquipmentCount.getText().toString());
        smartEquipment.strUnit = _txtWorkViewEquipmentUnit.getText().toString();
        smartEquipment.strMemo = _txtWorkViewEquipmentMemo.getText().toString();

        SmartSingleton.smartWork.arrSmartEquipments.add(smartEquipment);
        adapterEquipment.notifyItemRangeInserted(adapterEquipment.getItemCount(), 1);

        _txtWorkViewEquipmentCate1.setText("");
        _txtWorkViewEquipmentCate2.setText("");
        _txtWorkViewEquipmentCount.setText("");
        _txtWorkViewEquipmentUnit.setText("");
        _txtWorkViewEquipmentMemo.setText("");

        _txtWorkViewEquipmentMemo.requestFocus();
        downKeyboard(this, _txtWorkViewEquipmentMemo);
    }

    public static void downKeyboard(Context context, EditText editText) {
        InputMethodManager mInputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
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

            // 쓰기 권한 체크
            if( !pref.getValue("pref_user_type","").equals("employee") ) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this).content("현장소장만 등록이 가능합니다.").positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        }).show();
                return true;
            }

            if( _txtBuildName.getText().toString().trim().equals("") ) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this).content("현장명을 먼저 선택해 주세요.").positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        }).show();
                return true;
            }
            if( _txtDate.getText().toString().trim().equals("") ) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this).content("작업일을 먼저 입력해 주세요.").positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        }).show();
                return true;
            }
            if( _txtMemo.getText().toString().trim().equals("") ) {
                new MaterialDialog.Builder(SmartWorkViewActivity.this).content("특기사항을 먼저 입력해 주세요.").positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        }).show();
                return true;
            }

            // 프로그래스 실행 !!
            showIndeterminateProgressDialog(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //
    private void showIndeterminateProgressDialog(boolean horizontal) {

        if(true) {
            md = new MaterialDialog.Builder(this)
                    .title("스마트일보 전송중")
                    .content("스마트일보 전송중 입니다..")
                    .progress(true, 0)
                    .progressIndeterminateStyle(horizontal)
                    //.cancelable(false)
                    .show();
            registWork();
        } else {

        }
    }
    private void registWork() {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<ResponseBody> call = smartService.registWork(SmartSingleton.smartWork.strBuildCode, SmartSingleton.smartWork);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ResponseBody responses = response.body();

                    if( !responses.toString().equals("") ) {

                    } else {
                        Snackbar.make(SmartWorkActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responses : 데이터가 정확하지 않습니다.");
                }

                md.dismiss();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                //Log.d("Error", t.getMessage());

                md.dismiss();
            }
        });
        /******************************************************************************************/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 1001: // 내용 수정 페이지에서 온 경우 내용 새로 고침
                /*
                _txtWriter.setText(SmartSingleton.smartOrder.strUserId);
                _txtDate.setText(SmartSingleton.smartOrder.datWrite);
                _txtContent.setHtml(SmartSingleton.smartOrder.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));

                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_order_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                break;
                */
            case 2001: // 사진 추가하기

                if( data != null) {
                    final String intId = data.getStringExtra("intId");
                    final String strURL = data.getStringExtra("strURL");
                    final String strName = data.getStringExtra("strName");
                    final String strThumbnail = data.getStringExtra("strThumbnail");
                    final String strBuildCode = data.getStringExtra("strBuildCode");
                    final String strBuildName = data.getStringExtra("strBuildName");
                    final String strLavorCode = data.getStringExtra("strLavorCode");
                    final String strLocation = data.getStringExtra("strLocation");
                    final String strMemo = data.getStringExtra("strMemo");
                    final String datRegist = data.getStringExtra("datRegist");

                    final SmartPhoto smartPhoto = new SmartPhoto();
                    smartPhoto.intId = Integer.parseInt(intId);
                    smartPhoto.strURL = strURL;
                    smartPhoto.strName = strName;
                    smartPhoto.strThumbnail = strThumbnail;
                    smartPhoto.strBuildCode = strBuildCode;
                    smartPhoto.strBuildName = strBuildName;
                    smartPhoto.strLavorCode = strLavorCode;
                    smartPhoto.strLocation = strLocation;
                    smartPhoto.strMemo = strMemo;
                    smartPhoto.datRegist = datRegist;

                    SmartSingleton.smartWork.arrSmartPhotos.add(smartPhoto);
                    adapterPhoto.notifyItemRangeInserted(adapterPhoto.getItemCount(), 1);

                    scrollView = (ScrollView) findViewById(R.id.sv_work_view);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
                break;
        }
    }

}
