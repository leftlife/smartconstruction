package kr.koogle.android.smartconstruction;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import butterknife.Bind;
import butterknife.ButterKnife;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBBSClient;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetter;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartClientViewActivity extends AppCompatActivity {
    private static final String TAG = "SmartWorkViewActivity";
    private RbPreference pref;

    @Bind(R.id.txt_client_view_title) TextView _txtTitle;
    @Bind(R.id.txt_client_view_writer) TextView _txtWriter;
    @Bind(R.id.txt_client_view_date) TextView _txtDate;
    @Bind(R.id.txt_client_view_content) TextView _txtContent;

    private String clientCode;
    private SmartBBSClient bbsClient;

    // HTML imageGetter
    private HashMap<String, Drawable> mImageCache = new HashMap<String, Drawable>();
    private String mDescription = "...your html here...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_view);
        ButterKnife.bind(this);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartClientViewActivity.this.finish();
            }
        });

        // 리스트 클릭시 넘어온값 받기 !!
        clientCode = String.valueOf(getIntent().getExtras().getInt("intId"));
        bbsClient = new SmartBBSClient();
        /******************************************************************************************/
        if(bbsClient.intId == 0) {
            // Labor Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<SmartBBSClient> call = smartService.getSmartBBSClient(clientCode);

            call.enqueue(new Callback<SmartBBSClient>() {
                @Override
                public void onResponse(Call<SmartBBSClient> call, Response<SmartBBSClient> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        bbsClient = response.body();

                        if (bbsClient.intId != 0) {
                            _txtTitle.setText(bbsClient.strTitle);
                            _txtWriter.setText(bbsClient.strWriter);
                            _txtDate.setText(bbsClient.datWrite);

                            /*
                            HtmlRemoteImageGetter imgGetter = new HtmlRemoteImageGetter(_txtContent, bbsClient.strContent);
                            _txtContent.setText(Html.fromHtml(bbsClient.strContent, imgGetter, null));
                            */
                            _txtContent.setText(bbsClient.strContent);

                            Toast.makeText(SmartClientViewActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    } else {
                        Toast.makeText(SmartClientViewActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseLaborCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<SmartBBSClient> call, Throwable t) {
                    Toast.makeText(SmartClientViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/

    }

}
