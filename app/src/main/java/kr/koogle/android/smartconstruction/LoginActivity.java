package kr.koogle.android.smartconstruction;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Bind;
import kr.koogle.android.smartconstruction.http.AccessToken;
import kr.koogle.android.smartconstruction.http.LoginService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.User;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            */
            }
        });
    }

    public void login() {
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();
        Log.d(TAG, "Login : " + email + "/" + password);

        if (!validate()) {
            onLoginFailed();
            return;
        }
        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("로그인 중 입니다.");
        progressDialog.show();

        /******************************************************************************************/
        // get access token
        Log.d(TAG, "loginService.getLoginToken 실행!!");
        LoginService loginService = ServiceGenerator.createService(LoginService.class, email, password);
        Call<User> call = loginService.getLoginToken();

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // get raw response
                // okhttp3.Response raw = response.raw();
                if (response.isSuccessful() && response.body() != null ) {
                    final User user = response.body();
                    // 폰에 accessToken 값 저장
                    String credentials = user.getId() + ":" + user.getAccessToken();
                    final String basicToken = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                    Log.d(TAG, "AccessToken : " + basicToken);
                    RbPreference pref = new RbPreference(getBaseContext());
                    pref.put("code", user.getCode());
                    pref.put("id", user.getId());
                    pref.put("type", user.getType());
                    pref.put("group", user.getGroup());
                    pref.put("accessToken", basicToken);
                    pref.put("phone", user.getPhone());

                    onLoginSuccess();
                } else {
                    // error response, no access to resource?
                    Toast.makeText(getBaseContext(), "로그인 정보가 정확하지 않습니다." , Toast.LENGTH_SHORT).show();
                }

                _loginButton.setEnabled(true);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // something went completely south (like no internet connection)
                Toast.makeText(getBaseContext(), "네트워크 상태가 좋지 않습니다!!" , Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                _loginButton.setEnabled(true);
                progressDialog.dismiss();
            }
        });
        /******************************************************************************************/
        // TODO: Implement your own authentication logic here.
        /*
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "다시 확인하신 후 입력해 주세요.", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || email.length() < 4 || email.length() > 10) {
            _emailText.setError("유효한 아이디를 입력하세요!");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("4자 이상 10자 이하의 비밀번호를 입력하세요!");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
