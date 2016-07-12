package kr.koogle.android.smartconstruction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import kr.koogle.android.smartconstruction.http.LoginService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.User;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends Activity {

    private static final String TAG = "IntroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        RbPreference pref = new RbPreference(getApplicationContext());

        /******************************************************************************************/
        // pref_access_token 값이 일치하는지 메인에서 한번 확인
        Log.d(TAG, "pref_access_token : " + pref.getValue("pref_access_token", ""));
        LoginService loginService = ServiceGenerator.createService(LoginService.class, pref.getValue("pref_access_token", ""));
        Call<User> call = loginService.checkLoginToken( "token" );

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    final User user = response.body();

                    Log.d(TAG, "pref_access_token : " + user.getPref_access_token());
                    startMainActivity();
                } else {
                    Toast.makeText(getBaseContext(), "회원정보가 정확하지 않습니다." , Toast.LENGTH_SHORT).show();
                    startLoginActivity();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getBaseContext(), "네트워크 연결이 지연되고 있습니다." , Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error" + t.getMessage());
                startLoginActivity();
            }
        });
        /******************************************************************************************/

        //startLoading();
    }

    private void startMainActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

    private void startLoginActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}