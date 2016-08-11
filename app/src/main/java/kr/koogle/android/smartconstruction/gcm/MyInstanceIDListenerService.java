package kr.koogle.android.smartconstruction.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import kr.koogle.android.smartconstruction.util.RbPreference;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyInstanceIDListerner";

    /**
     * 새로 토큰이 업데이트 되었을 때 여기로 Callback이 옵니다.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Settings 값 !!
        RbPreference pref = new RbPreference(getApplicationContext());

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        pref.put("pref_fcm_token", refreshedToken);
        // 이제 이 이후에 Token을 등록하는 코드를 넣어주세요.

    }

}
