package com.checker.xposed_hook;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHook implements IXposedHookLoadPackage {
    // 待检测的包名
    private static final String HOOK_PACKAGE_NAME = "com.sina.weibo";
    // 是否打印调用堆栈
    private static final boolean PRINT_STACK_TRACE = true;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("包名：-->> " + lpparam.packageName);

        /*过滤hook的包名*/
//        if (!HOOK_PACKAGE_NAME.equals(lpparam.packageName)) {
//            return;
//        }

        // 获取签名信息
        // getSigningInfo(lpparam);

        // 可用于屏蔽签名校验
        rewriteMetaData(lpparam, "AppSignature", "You Are Hooked");

        // 检查敏感信息获取
        checkPrivateInfoApis(lpparam);
    }

    /**
     * 获取签名
     *
     * @param lpparam
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void getSigningInfo(XC_LoadPackage.LoadPackageParam lpparam) {
        // 获取签名信息：抽象方法需要找到其实现类来hook
        XposedHelpers.findAndHookMethod(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader,
                "getPackageInfo",
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("\n获取打包信息：-->> packageInfo = " + param.getResult());
                        PackageInfo packageInfo = (PackageInfo) param.getResult();
                        XposedBridge.log("\n获取签名信息：-->> packageInfo.signingInfo = " + packageInfo.signingInfo);
                        super.afterHookedMethod(param);
                    }
                });
    }

    /**
     * 重写Meta数据
     *
     * @param lpparam
     * @param key
     * @param value
     */
    private void rewriteMetaData(XC_LoadPackage.LoadPackageParam lpparam, String key, String value) {
        XposedHelpers.findAndHookMethod(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader,
                "getApplicationInfo",
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ApplicationInfo applicationInfo = (ApplicationInfo) param.getResult();
//                        XposedBridge.log("\n获取Meta数据：-->> metaData = " + applicationInfo.metaData);
//                        XposedBridge.log("\n获取Meta数据：-->> " + key + " = " + applicationInfo.metaData.get(key));
                        // 修改数据
                        applicationInfo.metaData.putString(key, value);
//                        XposedBridge.log("\n修改Meta数据：-->> " + key + " = " + value);
                    }
                });
    }

    private void checkPrivateInfoApis(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("开始检测：-->> ");
        // MAC地址
        XposedHelpers.findAndHookMethod(
                android.net.wifi.WifiInfo.class.getName(),
                lpparam.classLoader,
                "getMacAddress",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取MAC地址：调用WifiInfo.getMacAddress()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                }
        );

        // MAC地址
        XposedHelpers.findAndHookMethod(
                java.net.NetworkInterface.class.getName(),
                lpparam.classLoader,
                "getHardwareAddress",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取MAC地址：调用NetworkInterface.getHardwareAddress()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                }
        );

        // IP地址：WiFi
        XposedHelpers.findAndHookMethod(
                android.net.wifi.WifiInfo.class.getName(),
                lpparam.classLoader,
                "getIpAddress",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取IP地址（WiFi）：调用WifiInfo.getIpAddress()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // IP地址：Mobile
        XposedHelpers.findAndHookMethod(
                java.net.NetworkInterface.class.getName(),
                lpparam.classLoader,
                "getInetAddresses",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取IP地址（Mobile）：调用NetworkInterface.getInetAddresses()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // IMEI
        XposedHelpers.findAndHookMethod(
                android.telephony.TelephonyManager.class.getName(),
                lpparam.classLoader,
                "getDeviceId",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取IMEI：调用TelephonyManager.getDeviceId()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // IMSI
        XposedHelpers.findAndHookMethod(
                android.telephony.TelephonyManager.class.getName(),
                lpparam.classLoader,
                "getSubscriberId",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取IMSI：调用TelephonyManager.getSubscriberId()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // 获取安装列表：抽象方法需要找到其实现类来hook
        XposedHelpers.findAndHookMethod(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader,
                "getInstalledPackages",
                int.class,
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取安装列表：调用PackageManager.getInstalledPackages(int)");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // Android ID
        XposedHelpers.findAndHookMethod(
                "android.provider.Settings.Secure",
                lpparam.classLoader,
                "getString",
                ContentResolver.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        if (Settings.Secure.ANDROID_ID.equals(param.args[1])) {
                            XposedBridge.log("\n获取Android ID：调用Secure.getString(ContentResolver, String) where String==Secure.ANDROID_ID");
                            if (PRINT_STACK_TRACE) {
                                XposedBridge.log(getMethodStack());
                            }
                        }
                    }
                }
        );

        // 设备硬件序列号SN
        XposedHelpers.findAndHookMethod(
                Build.class.getName(),
                lpparam.classLoader,
                "getSerial",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取设备硬件序列号SN：调用Build.getSerial()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                }
        );

        // SIM卡序列号
        XposedHelpers.findAndHookMethod(
                android.telephony.TelephonyManager.class.getName(),
                lpparam.classLoader,
                "getSimSerialNumber",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取SIM卡序列号：调用TelephonyManager.getSimSerialNumber()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                });

        // 获取SD卡挂载状态
        XposedHelpers.findAndHookMethod(
                Environment.class.getName(),
                lpparam.classLoader,
                "getExternalStorageState",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取SD卡挂载状态：调用Environment.getExternalStorageState()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                }
        );

        // 获取正在运行的所有应用程序进程
        XposedHelpers.findAndHookMethod(
                ActivityManager.class.getName(),
                lpparam.classLoader,
                "getRunningAppProcesses",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("\n获取正在运行的所有应用程序进程：调用ActivityManager.getRunningAppProcesses()");
                        if (PRINT_STACK_TRACE) {
                            XposedBridge.log(getMethodStack());
                        }
                    }
                }
        );
    }

    private String getMethodStack() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();

        for (StackTraceElement temp : stackTraceElements) {
            stringBuilder.append(temp.toString() + "\n");
        }

        return stringBuilder.toString();
    }
}
