package kr.koogle.android.smartconstruction;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.ZoomStyle;
import com.commonsware.cwac.security.RuntimePermissionUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class CameraPicActivity extends AppCompatActivity {
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

    private ViewFlipper wizardBody;
    private Button previous;
    private Button next;
    private File testRoot;
    private File testZip;
    private RuntimePermissionUtils utils;
    private File previewFrame;
    private boolean isVideo=false;

    private TextView txtContent;
    private ImageView imgContent;
    private Bitmap myBitmap;

    private static ImageView imgPicture;

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);

        // ToolBar 관련
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_camera_pic);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Cannot access external storage!", Toast.LENGTH_LONG).show();
            finish();
        }

        previewFrame= new File(getExternalCacheDir(), "cam2-preview.jpg");
        //Toast.makeText(this, previewFrame.getAbsolutePath(), Toast.LENGTH_LONG).show();

        // 퍼미션 체크 요청 유틸 !!
        utils = new RuntimePermissionUtils(this);

        /*
        wizardBody=(ViewFlipper)findViewById(R.id.wizard_body);
        previous=(Button)findViewById(R.id.previous);
        next=(Button)findViewById(R.id.next);
        */

        if (savedInstanceState==null) { // 처음 앱이 실행될 때 !!
            String filename="cam2_"+ Build.MANUFACTURER+"_"+Build.PRODUCT
                    +"_"+new SimpleDateFormat("yyyyMMdd'-'HHmmss").format(new Date());

            filename=filename.replaceAll(" ", "_");

            testRoot=new File(getExternalFilesDir(null), filename);

            String baseDir = testRoot.getAbsolutePath();
            //Toast.makeText(this, baseDir, Toast.LENGTH_LONG).show();
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
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


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
        */
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                Uri uri = data.getData();
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imgContent.setImageBitmap(myBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imgContent.setImageBitmap(myBitmap);

        }
    }

}