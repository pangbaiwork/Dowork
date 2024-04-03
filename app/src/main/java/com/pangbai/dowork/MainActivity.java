package com.pangbai.dowork;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pangbai.dowork.databinding.ActivityMainBinding;
import com.pangbai.dowork.fragment.RootfsInstallFragment;
import com.pangbai.dowork.tool.Init;
import com.pangbai.dowork.tool.util;
import com.pangbai.view.dialogUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavController.OnDestinationChangedListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final int REQUEST_CODE_FLOATING_WINDOW = 1001;
    private ActivityMainBinding binding;
    NavController ctr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.fullScreen(getWindow(), false);
        ensureWindowPermission();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_fragment);
        ctr = host.getNavController();
        NavigationUI.setupWithNavController(binding.navView, ctr);

// 在需要的地方检查是否已经启动



        // host.getNavController().addOnDestinationChangedListener();
        // ctr.popBackStack(ctr.getGraph().getStartDestination(),false);
    }


    @Override
    public void onClick(View view) {
    }


    public void ensureWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                dialogUtils.showConfirmationDialog(this,
                        "权限申请以及隐私协议",
                        "为运行必要服务,请授予本软件权限\n隐私协议:\n为运行浮窗,导入容器,chroot等操作本软件需要必要权限\n本软件承诺不收集任何用户私人信息, 权限仅用于软件服务内容",
                        "同意",
                        "退出",
                        () -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_LONG).show();
                            startActivityForResult(intent, REQUEST_CODE_FLOATING_WINDOW);
                        },
                        () -> finish());
            } else {
                // 已经有悬浮窗权限，可以在此处理相关逻辑
                new Init(MainActivity.this);
            }
        } else {
            // Android版本低于M，无需申请悬浮窗权限
            new Init(MainActivity.this);
        }
    }

    /////////////////////////////////////////////悬浮窗权限回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FLOATING_WINDOW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 用户已授予悬浮窗权限，可以在此处理相关逻辑
                    new Init(MainActivity.this);
                } else {
                    // 用户未授予悬浮窗权限，可以在此处理相关逻辑
                    finish();
                }
            }

        }
    }


    @Override
    public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {

    }
}

