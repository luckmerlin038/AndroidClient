package com.merlin.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.merlin.bean.ClientMeta;
import com.merlin.protocol.Tag;

public class MainActivity extends Activity implements Tag {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=new Intent(this,FileBrowserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        meta.setAccount("linqiang");
//        meta.setDeviceType(TAG_NAS_DEVICE);

        intent=new Intent(this,MediaSheetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        intent=new Intent(this,MediaSheetDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

        intent=new Intent(this,TransportActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();
//        String ddd="/storage/sdcard0/storage/emulated/0/linqiang.mp3";
//        MediaPlayer player=new MediaPlayer();
//        try {
//            InputStream fi= getAssets().open("linqiang.mp3");
//            byte[] bdd= new byte[1024*1024*5];
//            int size=fi.read(bdd);
//            player.play(bdd,0,size);
//        } catch (Exception e) {
//            Debug.E(getClass(),"ee"+e,e);
//            e.printStackTrace();
//        }
//        player.play();
        requestOverlayPermission(this);
    }

    public void requestOverlayPermission(Activity context) {
        if (null!=context){
//            final AlertDialog dialog = new AlertDialog.Builder(context)
//                    .setTitle("权限申请")
//                    .setMessage("哈哈权限")
//                    .setPositiveButton("确认", (dlg,which)->{
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
//                            context.startActivity(intent);
//                        }
//                        dlg.dismiss();
//                    }).setNegativeButton("取消", (dlg,which)->dlg.dismiss())
//                    .setCancelable(false)
//                    .create();
//            dialog.show();
        }
    }


}
