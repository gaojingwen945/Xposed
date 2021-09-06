package com.checker.xposed_hook;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                testGetSigningInfo();

                String result = testGetMetaData("AppSignature");
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void testGetSigningInfo() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SigningInfo signingInfo = packageInfo.signingInfo;
        // 在VirtualXposed中获取到的signingInfo为空
        // Signature[] signatures = signingInfo.getApkContentsSigners();
    }

    private String testGetMetaData(String name) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e) {
        }
        if (applicationInfo == null) {
            return "";
        }
        if (applicationInfo.metaData == null) {
            return "";
        }
        Object value = applicationInfo.metaData.get(name);
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
