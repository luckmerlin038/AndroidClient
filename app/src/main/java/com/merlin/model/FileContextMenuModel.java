package com.merlin.model;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.merlin.bean.FileMeta;
import com.merlin.client.R;
import com.merlin.client.databinding.FileContextMenuBinding;
import com.merlin.debug.Debug;
import com.merlin.file.Permissions;
import com.merlin.view.OnTapClick;

public class FileContextMenuModel extends Model implements OnTapClick {

    @Override
    protected void onRootAttached(View root) {
        ViewDataBinding binding= null!=root? DataBindingUtil.getBinding(root):null;
        if (null!=binding&&binding instanceof FileContextMenuBinding){
            FileContextMenuBinding fcmb=(FileContextMenuBinding)binding;
            FileMeta meta=fcmb.getFile();
            if (null!=meta){
                int permissions=meta.getPermissions();
                Permissions per=new Permissions();
                fcmb.setDeleteEnable(per.isOtherReadable(permissions)&&per.isOtherWriteable(permissions));
            }
        }
    }

    @Override
    public boolean onTapClick(View view, int clickCount, int resId, Object data) {
        switch (clickCount){
            case 1://Single tap
                if (null!=data&&data instanceof FileMeta){
                    onFileMetaClick(view,resId,(FileMeta)data);
                    return true;
                }
                break;
        }
        return false;
    }

    private void onFileMetaClick(View view,int resId,FileMeta meta){
        if (null!=meta){
            switch (resId){
                case R.string.delete:
                    toast("删除 "+meta.getTitle());
                    break;
                case R.string.move:
                    toast("移动 "+meta.getTitle());
                    break;
                case R.string.rename:
                    toast("重命名 "+meta.getTitle());
                    break;
            }
        }
    }
}