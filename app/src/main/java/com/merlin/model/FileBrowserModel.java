package com.merlin.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.ObservableField;

import com.merlin.activity.ConveyorActivity;
import com.merlin.adapter.ListAdapter;
import com.merlin.api.Address;
import com.merlin.api.CoverMode;
import com.merlin.api.Label;
import com.merlin.api.PageData;
import com.merlin.api.Reply;
import com.merlin.bean.ClientMeta;
import com.merlin.bean.FolderData;
import com.merlin.bean.LocalFile;
import com.merlin.bean.NasFile;
import com.merlin.browser.Collector;
import com.merlin.browser.LocalFileCollector;
import com.merlin.browser.NasFileCollector;
import com.merlin.client.R;
import com.merlin.client.databinding.ClientDetailBinding;
import com.merlin.client.databinding.DeviceTextBinding;
import com.merlin.client.databinding.FileBrowserMenuBinding;
import com.merlin.conveyor.ConveyorService;
import com.merlin.debug.Debug;
import com.merlin.browser.FileBrowser;
import com.merlin.browser.LocalFileBrowser;
import com.merlin.browser.NasFileBrowser;
import com.merlin.protocol.Tag;
import com.merlin.view.OnLongClick;
import com.merlin.view.OnTapClick;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class FileBrowserModel extends Model implements Label, Tag, OnTapClick, OnLongClick, Model.OnActivityResume,Model.OnActivityBackPress {
    private final Map<String,FileBrowser> mAllClientMetas=new HashMap<>();
    private final ObservableField<Integer> mClientCount=new ObservableField<>();
    private final ObservableField<FileBrowser> mCurrent=new ObservableField<>();
    private final ObservableField<FolderData> mCurrentFolder=new ObservableField<>();
    private final ObservableField<Integer> mCurrentMode=new ObservableField<>(FileBrowser.MODE_NORMAL);
    private final ObservableField<ListAdapter> mCurrentAdapter=new ObservableField<>();
    private final ObservableField<ClientMeta> mCurrentMeta=new ObservableField<>();
    private final ObservableField<String> mCurrentMultiChooseSummary=new ObservableField<>();

    private final FileBrowser.Callback mBrowserCallback=new FileBrowser.Callback() {
        @Override
        public void onFolderPageLoaded(PageData page, String debug) {
            mCurrentFolder.set(null!=page&&page instanceof FolderData?(FolderData)page:null);
        }

        @Override
        public boolean onTapClick(View view, int clickCount, int resId, Object data) {
            return FileBrowserModel.this.onTapClick(view,clickCount,resId,data);
        }

        @Override
        public int getMode() {
            return mCurrentMode.get();
        }
    };
    private Collector mProcessing;

    private interface Api{
        @POST(Address.PREFIX_USER+"/client/meta")
        Observable<Reply<ClientMeta>> queryClientMeta();

        @POST(Address.PREFIX_FILE+"/sync/check")
        @FormUrlEncoded
        Observable<Reply> checkFileSync(@Field(Label.LABEL_MD5) String md5s);
    }

    @Override
    protected void onRootAttached(View root) {
        super.onRootAttached(root);
        putClientMeta(ClientMeta.buildLocalClient(getContext()), "After mode create.");
        ClientMeta testClient=new ClientMeta("算法","",Address.URL,"",null,"///");
//        ClientMeta testClient=new ClientMeta("算法","/volume1",Address.URL,null,"","/");
        putClientMeta(testClient, "After mode create.");
//        refreshClientMeta("After mode create.");
//        call(prepare(Api.class, Address.URL).checkFileSync("linqinagMD5"), new OnApiFinish<Void>() {
//            @Override
//            public void onApiFinish(int what, String note, Void data, Object arg) {
//                Debug.D(getClass(),"AAAAAAAA "+what+" "+note+" "+arg );
//            }
//        });
    }

    private boolean putClientMeta(ClientMeta meta,String debug){
        String url=null!=meta?meta.getUrl():null;
        if (null!=url&&url.length()>0){
            Map<String,FileBrowser> list=mAllClientMetas;
            Debug.D(getClass(),"Put client "+url+" "+(null!=debug?debug:"."));
            Context context=getViewContext();
            boolean local=meta.isLocalClient();
            list.put(url,local?new LocalFileBrowser(context,meta,mBrowserCallback):
                    new NasFileBrowser(context,meta,mBrowserCallback));
            mClientCount.set(list.size());
            changeDevice(meta,false,"After client put "+(null!=debug?debug:"."));
            return true;
        }
        Debug.W(getClass(),"Can't put client meta "+(null!=debug?debug:"."));
        return false;
    }

    private boolean changeDevice(ClientMeta client, boolean force, String debug){
        final String url=null!=client?client.getUrl():null;
        if (null!=url&&url.length()>0){
            if (force||null==mCurrent.get()){
                Debug.D(getClass(),"Change browser device "+client.getName()+" "+(null!=debug?debug:"."));
                Map<String,FileBrowser> map=mAllClientMetas;
                if (null!=map){
                    FileBrowser browser=map.get(url);
                    if (null!=browser){
                         FileBrowser curr=mCurrent.get();
                         mCurrentAdapter.set(browser);
                         PageData page=null!=browser?browser.getLastPage():null;
                         mCurrentFolder.set(null!=page&&page instanceof FolderData?(FolderData)page:null);
                         if (null==page){
                             browser.loadPage(client.getRoot(),"While browser switched.");
                         }
                         mCurrentMeta.set(browser.getMeta());
                         mCurrent.set(browser);
                         return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTapClick(View view, int clickCount, int resId, Object data)  {
        switch (clickCount){
            case 1:
                switch (resId){
                    case R.id.fileBrowser_deviceNameTV:
                        return (null!=view&&view instanceof TextView&&showClientMenu((TextView)view,
                                "After tap click."))||true;
                    case R.string.transportList:
                        return launchTransportList("After transport list tap click.");
                    case R.drawable.selector_menu:
                        return showBrowserMenu(view,"After tap click.");
                    case R.drawable.selector_back:
                        return onBackIconPressed(view,"After back pressed.");
                    case R.drawable.cancel_selector:
                        return !isMode(FileBrowser.MODE_NORMAL)&&entryMode(FileBrowser.MODE_NORMAL,null,
                                "After cancel tap click.");
                    case R.drawable.choose_all_selector:
                        return chooseAll(true,"After tap click.");
                    case R.drawable.ic_menu_alls:
                        return chooseAll(false,"After tap click.");
                    default:
                        break;
                }
                break;
            case 2:
                 switch (resId){
                     case R.id.fileBrowser_deviceNameTV:
                         return (null != view && null != data && data instanceof ClientMeta &&
                                 showClientDetail(view, (ClientMeta) data, "After tap click.")) || true;
                 }
        }
        switch (resId){
            case R.string.upload:
                Collector collector = mProcessing;
                return (isMode(FileBrowser.MODE_UPLOAD) ? upload(null != collector && collector
                        instanceof LocalFileCollector ? ((LocalFileCollector) collector).getFiles()
                        : null,CoverMode.tapClickCountToMode(clickCount), "After tap click.")
                        : prepareConveyFile(FileBrowser.MODE_UPLOAD, data, "After tap click."));
            case R.string.download:
                collector = mProcessing;
                return (isMode(FileBrowser.MODE_DOWNLOAD) ? download(null != collector && collector
                        instanceof NasFileCollector ? ((NasFileCollector) collector).getFiles()
                        : null,CoverMode.tapClickCountToMode(clickCount), "After tap click.")
                        : prepareConveyFile(FileBrowser.MODE_DOWNLOAD, data, "After tap click."));
        }
        FileBrowser model=getCurrentModel();
        return null!=model&&model.onTapClick(view,clickCount,resId,data);
    }

    private boolean upload(ArrayList<LocalFile> files,int coverMode,String debug){
        if (null==files||files.size()<=0){
            toast(R.string.noneDataToOperate);
            return false;
        }
        FolderData folder=mCurrentFolder.get();
        String folderPath=null!=folder?folder.getPath():null;
        ClientMeta meta=mCurrentMeta.get();
        if (null==folderPath||folderPath.length()<=0||null==meta||meta.isLocalClient()){
            toast(null==meta?R.string.canNotOperateHere:R.string.targetFolderInvalid);
            return false;
        }
        if (ConveyorService.upload(getViewContext(),files,meta,folderPath,coverMode,debug)){
            entryMode(FileBrowser.MODE_NORMAL,null,"After upload start succeed.");
            launchTransportList("");
            return toast(R.string.succeed)||true;
        }
        return toast(R.string.fail);
    }

    private boolean download(ArrayList<NasFile> files,int coverMode,String debug){
        if (null==files||files.size()<=0){
            toast(R.string.noneDataToOperate);
            return false;
        }
        FolderData folder=mCurrentFolder.get();
        String folderPath=null!=folder?folder.getPath():null;
        ClientMeta meta=mCurrentMeta.get();
        if (null==folderPath||folderPath.length()<=0||null==meta||!meta.isLocalClient()){
            toast(null==meta?R.string.canNotOperateHere:R.string.targetFolderInvalid);
            return false;
        }
        if (ConveyorService.download(getViewContext(),files,meta,folderPath,coverMode,debug)){
            entryMode(FileBrowser.MODE_NORMAL,null,"After download start succeed.");
            launchTransportList("");
            return toast(R.string.succeed)||true;
        }
        return toast(R.string.fail);
    }

    private boolean prepareConveyFile(int mode,Object obj,String debug){
        Collector collector=mProcessing;
        boolean succeed=false;
        switch (mode){
            case FileBrowser.MODE_UPLOAD:
                succeed=(null!=obj&& obj instanceof LocalFile&&(null!=collector&&collector instanceof
                        LocalFileCollector?collector: (collector=new LocalFileCollector(null))).add((LocalFile)obj));
                break;
            case FileBrowser.MODE_DOWNLOAD:
                succeed=(null!=obj&& obj instanceof NasFile&&(null!=collector&&collector instanceof
                        NasFileCollector?collector: (collector=new NasFileCollector(null))).add((NasFile)obj));
                break;
        }
        return succeed&&(isMode(mode)||entryMode(mode,collector,debug));
    }

    private boolean chooseAll(boolean choose,String debug){
        FileBrowser browser=getCurrentModel();
        return null!=browser&&isMode(FileBrowser.MODE_MULTI_CHOOSE)&& browser.chooseAll(choose,debug);
    }

    public final boolean isMode(int ...models){
        if (null!=models&&models.length>0){
            int curr=mCurrentMode.get();
            for (int mode:models) {
                if (mode==curr){
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean entryMode(int mode,Collector collector,String debug){
        mProcessing=collector;//Clean processing while each mode change
        if (!isMode(mode)){
            mCurrentMode.set(mode);
            return true;
        }
        return false;
    }

    private boolean showClientDetail(View view,ClientMeta meta,String debug){
        ClientDetailBinding binding=null!=view&&null!=meta?inflate(R.layout.client_detail):null;
        if (null!=binding){
            binding.setClient(meta);
            return showAtLocation(view,binding,Gravity.CENTER,0,0,null);
        }
        return false;
    }

    private boolean showClientMenu(TextView tv,String debug){
        Map<String,FileBrowser> map=mAllClientMetas;
        Context context=null!=tv?tv.getContext():null;
        Set<String> set=null!=map?map.keySet():null;
        final int size=null!=context&&null!=set?set.size():0;
        if (size<=1){
            toast(R.string.canNotSwitch);
            return false;
        }
        LinearLayout ll=new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        final OnTapClick click=( view, clickCount, resId, data)-> {
            return (null!=data&&data instanceof ClientMeta&&changeDevice((ClientMeta)data,true,"After device choose."))||true;
        };
        FileBrowser currentModel=mCurrent.get();
        ClientMeta current=null!=currentModel?currentModel.getMeta():null;
        for (String child:set) {
            Object object= null!=child?map.get(child):null;
            object=null!=object?object instanceof FileBrowser ?((FileBrowser)object).getMeta():object:null;
            ClientMeta meta=null!=object&&object instanceof ClientMeta?(ClientMeta)object:null;
            if (null!=meta&&(null==current||!current.equals(meta))){
                DeviceTextBinding binding=inflate(R.layout.device_text);
                View root=null!=binding?binding.getRoot():null;
                if (null!=root){
                    binding.setDevice(meta);
                    ll.addView(root);
                }
                continue;
            }
        }
        return showAsDropDown(tv,ll,0,0,click,null);
    }

    private boolean showBrowserMenu(View view,String debug){
        FileBrowserMenuBinding binding=null!=view?inflate(R.layout.file_browser_menu):null;
        if (null!=binding){
            binding.setFolder(mCurrentFolder.get());
            binding.setMode(getMode().get());
            binding.setClient(getCurrentModelMeta());
            return showAtLocationAsContext(view,binding);
        }
        return false;
    }

    private boolean launchTransportList(String debug){
        Context context=getContext();
        if (null!=context){
            Intent intent=new Intent(context, ConveyorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View view, int clickCount, int resId, Object data) {
        FileBrowser model=getCurrentModel();
        return null!=model&&model instanceof OnLongClick&&((OnLongClick)model).onLongClick(view,clickCount,resId,data);
    }

    @Override
    public boolean onActivityBackPressed(Activity activity) {
        return !isMode(FileBrowser.MODE_NORMAL,FileBrowser.MODE_UPLOAD,FileBrowser.MODE_DOWNLOAD)?
                entryMode(FileBrowser.MODE_NORMAL,null,"After activity  back press."):
                browserParent("After back pressed called.");
    }

    @Override
    public void onActivityResume(Activity activity, Intent intent) {
          FileBrowser model=getCurrentModel();
          if (null!=model&&model instanceof OnActivityResume){
              ((OnActivityResume)model).onActivityResume(activity,intent);
          }
    }

    public final boolean onBackIconPressed(View view, String debug){
        return browserParent(debug);
    }

    private boolean browserParent(String debug){
        FileBrowser browser=getCurrentModel();
        return null!=browser&&browser.browserParent(debug);
    }

    private ClientMeta getCurrentModelMeta(){
        FileBrowser model=getCurrentModel();
        return null!=model?model.getMeta():null;
    }

    private FileBrowser getCurrentModel(){
       return mCurrent.get();
    }

    public ObservableField<Integer> getMode() {
        return mCurrentMode;
    }

    public ObservableField<FolderData> getCurrentFolder(){
        return mCurrentFolder;
    }

    public ObservableField<ListAdapter> getCurrentAdapter() {
        return mCurrentAdapter;
    }

    public ObservableField<ClientMeta> getCurrentMeta() {
        return mCurrentMeta;
    }

    public ObservableField<String> getCurrentMultiChooseSummary() {
        return mCurrentMultiChooseSummary;
    }

    public ObservableField<Integer> getClientCount() {
        return mClientCount;
    }
}
