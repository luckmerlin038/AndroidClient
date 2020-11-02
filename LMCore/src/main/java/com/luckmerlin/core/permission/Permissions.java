package com.luckmerlin.core.permission;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public final class Permissions {
//       <uses-permission android:name="android.permission.CAMERA" />
//    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
//    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    public boolean isGranted(Context context,String ... permissions){
        return isGrantedPackage(context,null,permissions);
    }

    public boolean isGrantedPackage(Context context,String pkgName, String ... permissions){
        if (null==permissions||permissions.length<=0){
            return false;
        }
        PackageManager manager=null!=context?context.getPackageManager():null;
        if (null==manager){
            return false;
        }
        pkgName=null!=pkgName?pkgName:context.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String child:permissions){
                if (null!=child&&child.length()>0&&PackageManager.PERMISSION_GRANTED!=manager.checkPermission(child,pkgName)){
                    return false;
                }
            }
            return true;
        }
        try {
            PackageInfo pack = manager.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = null!=pack?pack.requestedPermissions:null;
            if (null==permissionStrings||permissionStrings.length<=0){
                return false;
            }
            for (String child:permissions){
                if (null!=child&&child.length()>0){
                    boolean granted=false;
                    for (String permission:permissionStrings) {
                        if (null!=permission&&permission.equals(child)){
                            granted=true;
                            break;
                        }
                    }
                    if (!granted){
                        return false;
                    }
                }
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public boolean ActivityCompat(Context context,String pkgName,String ... permissions){
        if (null==permissions||permissions.length<=0){
            return false;
        }
        PackageManager manager=null!=context?context.getPackageManager():null;
        if (null==manager){
            return false;
        }
        pkgName=null!=pkgName?pkgName:context.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity instanceof RequestPermissionsRequestCodeValidator) {
                ((RequestPermissionsRequestCodeValidator) activity)
                        .validateRequestPermissionsRequestCode(requestCode);
            }
            activity.requestPermissions(permissions, requestCode);
        } else if (activity instanceof OnRequestPermissionsResultCallback) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final int[] grantResults = new int[permissions.length];

                    PackageManager packageManager = activity.getPackageManager();
                    String packageName = activity.getPackageName();

                    final int permissionCount = permissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        grantResults[i] = packageManager.checkPermission(
                                permissions[i], packageName);
                    }

                    ((OnRequestPermissionsResultCallback) activity).onRequestPermissionsResult(
                            requestCode, permissions, grantResults);
                }
            });
        }

        return false;
    }
}
