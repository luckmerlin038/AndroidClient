package com.merlin.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.merlin.activity.PhotoPreviewActivity;
import com.merlin.api.Address;
import com.merlin.api.ApiMap;
import com.merlin.api.Canceler;
import com.merlin.api.CoverMode;
import com.merlin.api.Label;
import com.merlin.api.OnApiFinish;
import com.merlin.api.PageData;
import com.merlin.api.Reply;
import com.merlin.api.What;
import com.merlin.bean.ClientMeta;
import com.merlin.bean.Document;
import com.merlin.bean.FolderData;
import com.merlin.bean.ILocalFile;
import com.merlin.bean.IPath;
import com.merlin.client.R;
import com.merlin.client.databinding.LocalFileDetailBinding;
import com.merlin.debug.Debug;
import com.merlin.dialog.Dialog;
import com.merlin.util.Thumbs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class LocalFileBrowser extends FileBrowser{
    private final Md5Reader mMd5Reader=new Md5Reader();

    public interface Api{
        @POST(Address.PREFIX_FILE+"/sync/check")
        @FormUrlEncoded
        Observable<Reply<ApiMap<String,Reply<IPath>>>> checkSync(@Field(Label.LABEL_MD5) Collection<String> md5s);

        @POST(Address.PREFIX_FILE+"/none")
        Observable<Reply<ApiMap<String, IPath>>> deleteLocalFile();
    }

    public LocalFileBrowser(ClientMeta meta,Callback callback){
        super(meta,callback);
    }

    @Override
    protected Canceler onPageLoad(Object path, int from, OnApiFinish finish) {
        return null!=path&&path instanceof String?browserFolder((String)path,from,from+50,finish):null;
    }

    @Override
    public boolean onTapClick(View view, int clickCount, int resId, Object data) {
        return super.onTapClick(view, clickCount, resId, data);
    }

    private Canceler browserFolder(String path, int from, int to, OnApiFinish<Reply<PageData<ILocalFile>>> finish){
        path=null!=path&&path.length()>0?path:getClientRoot();
        File folder=null!=path&&path.length()>0?new File(path):null;
        final Reply<PageData<ILocalFile>>  reply=new Reply<>();
        Integer what=null;
        String note=null;
        Object arg=null;
        if (null==folder||folder.length()<=0){
            note=getText(R.string.pathInvalid);
            what=What.WHAT_ARGS_INVALID;
        }else if(from<0||to<=0){
            note=getText(R.string.inputNotNull);
            what=What.WHAT_ARGS_INVALID;
        }else if(!folder.exists()){
            note=getText(R.string.fileNotExist);
            what=What.WHAT_NOT_EXIST;
        }else if(!folder.exists()){
            note=getText(R.string.fileNotExist);
            what=What.WHAT_NOT_EXIST;
        }else if(!folder.isDirectory()){
            note=getText(R.string.pathInvalid);
            what=What.WHAT_NOT_DIRECTORY;
        } else if(!folder.canRead()){
            note=getText(R.string.nonePermission);
            what=What.WHAT_NONE_PERMISSION;
        }
        if (null==what){
            final File[] files=folder.listFiles();
            final int length=null!=files?files.length:0;
            FolderData folderData=new FolderData();
            folderData.setParent(folder.getParent());
            folderData.setPathSep(File.separator);
            folderData.setName(folder.getName());
            folderData.setFrom(from);
            folderData.setLength(length);
            reply.setData(folderData);
            if (from>=length){
                what=What.WHAT_OUT_OF_BOUNDS;
                note=getText(R.string.outOfBounds);
            }
            if (what==null){
                final int toIndex = Math.min(to,length);
                final int volume=toIndex-from;
                reply.setSuccess(true);
                reply.setWhat(What.WHAT_SUCCEED);
                Debug.D(getClass(),"Browsing local folders "+volume+" from "+from+" to "+toIndex+" "+path);
                if (volume>0){
                    final ArrayList<ILocalFile> list=new ArrayList(volume>1000?100:volume);
                    final Map<String,List<ILocalFile>> fileMaps=new HashMap<>();
                    Api api=prepare(Api.class, Address.URL, null);
                    final List<String> md5s=new ArrayList<>();
                    final String pathArg=path;
                    return call(api.checkSync(md5s).subscribeOn(Schedulers.io()).doOnSubscribe((Disposable disposable) ->{
                        final int maxAutoLoadMd5=1024*1024*50;
                        final Md5Reader md5Reader=mMd5Reader;
                        ILocalFile localFile;File child;String md5;
                        for (int i = from; i < toIndex; i++) {
                            if (null!=(localFile=null!=(child=files[i])?
                                    ILocalFile.build(child, null,child.length() <=maxAutoLoadMd5?md5Reader:null):null)){
                                if (list.add(localFile)&&null!=(md5=(null==localFile.getSync()?localFile.getMd5():
                                        null))&& md5.length()>0&&!md5s.contains(md5)&&md5s.add(md5)){
                                    List<ILocalFile> fileList=fileMaps.get(md5);
                                    if (null!=fileList){
                                        fileList.add(localFile);
                                    }else{
                                        fileList=new ArrayList<>();
                                        fileList.add(localFile);
                                        fileMaps.put(md5,fileList);
                                    }
                                }
                            }
                        }
                        folderData.setData(list);
                        reply.setSuccess(true);
                        reply.setWhat(What.WHAT_SUCCEED);
                        reply.setData(folderData);
                        if (null!=finish){
                            //test
//                            post(()->{      ArrayList<FileMeta> ddd=new ArrayList<>();
//                                ddd.add(list.get(0));
//                                deletePaths(ddd,getText(R.string.delete));},0);
                            //test end
                            post(()->finish.onApiFinish(reply.getWhat(),"Empty",reply, "List succeed."),0);
                        }
                        if (null==list||list.size()<=0){
                            disposable.dispose(); //Cancel to check file with server
                        }else{//Check file sync with server
                            //Do nothing
                        }
                    }),null,Schedulers.trampoline(),(OnApiFinish<Reply<ApiMap<String,Reply<IPath>>>>)
                            (int serverWhat, String serverNote, Reply<ApiMap<String, Reply<IPath>>> serverData, Object serverArg)->{
                        ApiMap<String,Reply<IPath>> map=null!=serverData?serverData.getData():null;
                        Set<String> serverSet=null!=map&&map.size()>0?map.keySet():null;
                        if (null!=serverSet&&serverSet.size()>0){
                            for (String childMd5:serverSet){
                                if (!isCurrentArgEquals(pathArg)){//If folder change
                                    break;
                                }
                                if (null!=childMd5){
                                    Reply<IPath> serverReply=map.get(childMd5);
                                    List<ILocalFile> localFiles=null!=childMd5?fileMaps.get(childMd5):null;
                                    if (null!=localFiles&&localFiles.size()>0){
                                        for (ILocalFile file:localFiles) {
                                            if (null!=file&&file.applySync(serverReply)){
                                                post(()->replace(file,"After sync check finish."),0);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    });
                }
                if (null!=finish){
                    finish.onApiFinish(reply.getWhat(),"None files matched.",reply,arg);
                }
                return (boolean cancel, String debug)-> false;
            }
        }
        reply.setSuccess(true);
        reply.setNote(note);
        reply.setWhat(null!=what?what:What.WHAT_INVALID);
        if (null!=finish){
            finish.onApiFinish(reply.getWhat(),note,reply,arg);
        }
        return null;
    }

    @Override
    protected boolean onShowPathDetail(Document meta, String debug) {
        Context context=getAdapterContext();
        if (null!=context&&null!=meta&&meta instanceof ILocalFile){
            String path=meta.getPath(null);
            File file=null!=path&&path.length()>0&&path.startsWith(File.separator)?new File(path):null;
            if (null!=file&&file.exists()){
                LocalFileDetailBinding binding=(LocalFileDetailBinding)inflate(R.layout.local_file_detail);
                binding.setFile(((ILocalFile)meta).getFile());
                binding.setMeta((ILocalFile) meta);
                String title=meta.getTitle();
                binding.setTitle(null!=title?title:file.getName());
                Dialog dialog=new Dialog(context);
                return dialog.setContentView(binding,true).setBackground(new Thumbs().getThumb(path)).title(file.getName()).show();
            }
            Debug.W(getClass(),"Can't show local file detail which not exist "+path+" "+(null!=debug?debug:"."));
            toast(R.string.fileNotExist);
            return false;
        }
        Debug.W(getClass(),"Can't show local file detail view arg is NULL "+(null!=debug?debug:"."));
        return false;
    }

    @Override
    protected boolean onSetAsHome(String path, OnApiFinish<Reply<String>> finish, String debug) {
        Context context=getAdapterContext();
        File file=null!=context&&null!=path&&path.length()>0&&path.startsWith(File.separator)?new File(path):null;
        boolean succeed=null!=file&&file.exists()&&file.isDirectory()&&new LocalBrowserHome().set(context,path);
        if (null!=finish){
            Reply<String> reply=new Reply<>(true,succeed?What.WHAT_SUCCEED:What.WHAT_FAIL_UNKNOWN,succeed?"Succeed":"Fail",path);
            finish.onApiFinish(reply.getWhat(),reply.getNote(),reply,null);
        }
        return true;
    }

    @Override
    protected boolean onOpenPath(Document meta, String debug) {
        ILocalFile localFile=null!=meta&&meta instanceof ILocalFile ?((ILocalFile)meta):null;
        String path=localFile.getPath(null);
        if (null!=path&&path.length()>0){
            final File file=new File(path);
            if (!file.exists()){
                return toast(R.string.fileNotExist)&&false;
            }
            Thumbs thumbs=new Thumbs();
            String extension=thumbs.getExtension(path);
            if (thumbs.isImageExtension(extension)){
                Intent intent=new Intent(getAdapterContext(), PhotoPreviewActivity.class);
                intent.putExtra(Label.LABEL_DATA, Uri.fromFile(file));
                return startActivity(intent);
            }
            String mimeType=null!=extension&&extension.length()>0? MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(extension):null;
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_VIEW);//动作，查看
            intent.setDataAndType(Uri.fromFile(file), mimeType);//设置类型
            startActivity(intent);
            return true;
        }
        return super.openPath(meta, debug);
    }

    @Override
    protected boolean onRenamePath(String path, String name, int coverMode, OnApiFinish<Reply<IPath>> finish, String debug) {
        Integer what=null;String note=null;boolean succeed=false;
        IPath data=null;Object arg=null;
        if (null==path||null==name||path.length()<=0||name.length()<=0){
            what=What.WHAT_ARGS_INVALID;
            note=getText(R.string.inputNotNull);
        }else{
           succeed=true;
           File file=new File(path);
           String currName=file.getName();
           int postfixIndex=null!=currName&&file.isFile()&&(coverMode&CoverMode.POSTFIX)==0?currName.lastIndexOf("."):-1;
           String postfix=postfixIndex>0?currName.substring(postfixIndex):null;
           name=null!=postfix&&postfix.length()>0?name+postfix:name;
           File folder=null!=name&&name.length()>0?file.getParentFile():null;
           File target=null!=folder?new File(folder,name):null;
           if (null==target){
               what=What.WHAT_ARGS_INVALID;
               note=getText(R.string.inputNotNull);
           }else if (!file.exists()){
               what=What.WHAT_FILE_NOT_EXIST;
               note=getText(R.string.fileNotExist);
           }else if (!file.canWrite()){
               what=What.WHAT_NONE_PERMISSION;
               note=getText(R.string.nonePermission);
           }else if (target.exists()&&(coverMode&CoverMode.REPLACE)==0){
               what=What.WHAT_FILE_EXIST;
               note=getText(R.string.fileAlreadyExist);
           }else if (target.exists()&&(target.delete()||target.exists())){
               what=What.WHAT_ERROR_UNKNOWN;
               note=getText(R.string.deleteFail);
           }else if(file.renameTo(target)&&target.exists()) {
               String[] fix= ILocalFile.getPostfix(target);
//               data=new Path(folder.getAbsolutePath(),fix[0],fix[1]);
               what=What.WHAT_SUCCEED;
               note=getText(R.string.succeed);
           }
        }
        return invokeFinish(succeed,what,note,finish,data,arg);
    }

    @Override
    protected boolean onCreatePath(boolean dir, int coverMode, String folder, String name, OnApiFinish<Reply<IPath>> finish, String debug) {
       Integer what=null;String note=null;boolean succeed=false;
        IPath modify=null;Object arg=null;
        if (null==folder||name==null||folder.length()<=0||name.length()<=0){
            what=What.WHAT_ARGS_INVALID;
            note=getText(R.string.inputNotNull);
        }else{
            final File file=new File(folder,name);
            final File parent=file.getParentFile();
            if (file.exists()&&(coverMode& CoverMode.REPLACE)==0){
                what=What.WHAT_FILE_EXIST;
                note=getText(R.string.fileAlreadyExist);
            }else if(null==parent||(!parent.exists()&&(!parent.mkdirs())||!parent.exists())){
                what=What.WHAT_ERROR_UNKNOWN;
                note=getText(R.string.createFail);
            }else if (!parent.canWrite()&&!parent.canExecute()){
                what=What.WHAT_NONE_PERMISSION;
                note=getText(R.string.nonePermission);
            }else if (file.exists()&&(!file.delete()||file.exists())){
                what=What.WHAT_ERROR_UNKNOWN;
                note=getText(R.string.deleteFail);
            }else{
                try {
                    succeed=dir?file.mkdir():file.createNewFile();
                    if (file.exists()) {
                        note=getText(R.string.createSucceed);
                        what=What.WHAT_SUCCEED;
                        String[] fix= ILocalFile.getPostfix(file);
//                        modify = new Path(folder, fix[0],fix[1]);
                        arg = ILocalFile.build(file,null,null);
                    }
                } catch (IOException e) {
                    what=What.WHAT_ERROR_UNKNOWN;
                    note=getText(R.string.createFail);
                }
            }
        }
        return invokeFinish(succeed,what,note,finish,modify,arg);
    }

    @Override
    protected boolean onReboot(String debug) {
        toast("本地 暂不支持重启");
        return true;
    }
}
