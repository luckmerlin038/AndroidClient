package com.merlin.model;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.databinding.ObservableField;

import com.merlin.adapter.BrowserAdapter;
import com.merlin.bean.ClientMeta;
import com.merlin.bean.File;
import com.merlin.bean.FileMeta;
import com.merlin.bean.FolderData;
import com.merlin.bean.NasFile;
import com.merlin.bean.NasFolder;
import com.merlin.client.R;
import com.merlin.view.OnLongClick;
import com.merlin.view.OnTapClick;

import java.util.List;

public class BrowserModel<T extends FileMeta> extends Model implements Model.OnActivityResume, FileBrowserModel.OnBrowserModelChange, OnTapClick, OnLongClick, Model.OnActivityBackPress {
    public final static int MODE_NORMAL=1212;
    public final static int MODE_MULTI_CHOOSE=1213;
    public final static int MODE_COPY=1214;
    public final static int MODE_MOVE=1215;
    private ClientMeta mClientMeta;
    private final ObservableField<FolderData> mCurrentFolder=new ObservableField();
    private final ObservableField<Integer> mMode=new ObservableField<>();
    private final ObservableField<Boolean> mAllChoose=new ObservableField<>();
    private final ObservableField<String> mMultiChooseSummary=new ObservableField<>();
    private BrowserAdapter<T> mBrowserAdapter;
    private Object mProcessing;

    public interface OnBrowserModelChange{
        void onBrowserModelChanged(Model last,Model current);
    }

    public BrowserModel(ClientMeta meta){
        mClientMeta=meta;
        entryMode(MODE_NORMAL,"After model create.");
    }

    protected final boolean setAdapter(BrowserAdapter<T> adapter){
        if (null!=adapter){
            mBrowserAdapter=adapter;
            browserPath("","After model adapter set.");
        }
        return false;
    }

    protected final boolean setMeta(ClientMeta meta,String debug){
        if (null!=meta){
            mClientMeta = meta;
            return true;
        }
        return false;
    }

    protected final boolean resetBrowserCurrentFolder(String debug){
        BrowserAdapter<T> adapter=mBrowserAdapter;
        return null!=adapter&&adapter.reset(debug);
    }

    private final boolean refreshCurrentPath(String debug){
        FolderData meta=mCurrentFolder.get();
        return browserPath(null!=meta?meta.getPath():null,debug);
    }

    protected boolean onBackIconPressed(String debug){
        return browserParent(debug);
    }

    private boolean browserPath(String pathValue, String debug){
        BrowserAdapter adapter=mBrowserAdapter;
        return null!=adapter&&adapter.loadPage(pathValue,debug);
    }

    private boolean browserParent(String debug){
        FolderData current=mCurrentFolder.get();
        String parent=null!=current?current.getParent():null;
        String curr=null!=current?current.getPath():null;
        if (null==parent||parent.length()<=0||(null!=curr&&curr.equals(parent))){
            toast(R.string.alreadyArrivedRoot);
            return false;
        }
        return browserPath(parent,debug);
    }

    /**
     * @deprecated
     * @return
     * @return
     */
    protected ClientMeta getMeta() {
        return getClientMeta();
    }

    public ClientMeta getClientMeta() {
        return mClientMeta;
    }

    public final ObservableField<Integer> getMode() {
        return mMode;
    }

    /**
     * @deprecated
     */
    protected final FolderData getCurrentFolderData() {
        return getCurrentFolder();
    }

    public final FolderData getCurrentFolder() {
        ObservableField<FolderData> data=mCurrentFolder;
        return null!=data?data.get():null;
    }

    public final ObservableField<String> getMultiChooseSummary() {
        return mMultiChooseSummary;
    }

    public final boolean isAllChoose(){
        return false;
    }

    @Override
    public boolean onTapClick(View view, int clickCount, int resId, Object data) {
        switch (clickCount){
            case 1:
                return onSingleTapClick(view,resId,data);
        }
        return false;
    }

    protected boolean onSingleTapClick(View view, int resId, Object data) {
        switch (resId){
            default:
                if (null!=data&&data instanceof FileMeta ){
                    FileMeta file=(FileMeta)data;
                    if (isMode(MODE_MULTI_CHOOSE)) {
                        BrowserAdapter adapter=getBrowserAdapter();
                        adapter.multiChoose(file);
                        return (null!=adapter&&adapter.multiChoose(file))||true;
                    }else if(file.isAccessible()){
                        if (file.isDirectory()) {
                            browserPath(file.getPath(), "After directory click.");
                        } else{//Open file
                        }
                    }else{
                        toast(R.string.nonePermission);
                    }
                }
        }
        return false;
    }

    @Override
    public boolean onLongClick(View view, int clickCount, int resId, Object data) {
        return false;
    }


    protected final boolean entryMode(int mode,String debug){
        if (!isMode(mode)){
            mProcessing=null;
            mMode.set(mode);
            BrowserAdapter adapter=mBrowserAdapter;
            if (null!=adapter){
                adapter.setMode(mode);
            }
            switch (mode){
                case MODE_MULTI_CHOOSE:
                    return refreshMultiChooseCount();
                case MODE_COPY:
                    break;
                case MODE_MOVE:
                    break;
            }
            return true;
        }
        return false;
    }

    protected final Object getProcessing() {
        return mProcessing;
    }


    protected final void setProcessing(Object processing,String debug) {
        this.mProcessing = processing;
    }

    protected final boolean isMode(int mode){
        ObservableField<Integer> current=mMode;
        Integer curr=null!=current?current.get():null;
        return null!=curr&&mode==curr;
    }

    @Override
    public void onBrowserModelChanged(Model last, Model current) {
        if (null!=current&&current==this){
            refreshCurrentPath("After browser model changed.");
        }
    }

    private boolean refreshMultiChooseCount() {
        FolderData folderMeta=mCurrentFolder.get();
        int length=null!=folderMeta?folderMeta.getLength():0;
        BrowserAdapter adapter=mBrowserAdapter;
        int count=null!=adapter?adapter.getChooseCount():0;
        mMultiChooseSummary.set(count<=0?"None selected(0/"+length+")":"Selected("+count+"/"+length+")");
        if (null!=adapter){
            List<FolderData> data=adapter.getData();
            int size=null!=data?data.size():0;
            mAllChoose.set(size==count&&size>0);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResume(Activity activity, Intent intent) {
        refreshCurrentPath("After activity onResume.");
    }

    @Override
    public boolean onActivityBackPressed(Activity activity) {
        if (!isMode(MODE_NORMAL)){
            return entryMode(MODE_NORMAL,"After activity  back press.");
        }
        return browserParent("After back pressed called.");
    }

    public BrowserAdapter<T> getBrowserAdapter() {
        return mBrowserAdapter;
    }
}
