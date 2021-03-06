package com.merlin.website;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import com.merlin.activity.LocalPhotoChooseActivity;
import com.merlin.adapter.WebsiteBannerAdapter;
import com.merlin.adapter.WebsiteCategoriesAdapter;
import com.merlin.api.Canceler;
import com.merlin.api.Label;
import com.merlin.api.OnApiFinish;
import com.merlin.api.PageData;
import com.merlin.api.Reply;
import com.merlin.bean.IPath;
import com.merlin.client.R;
import com.merlin.client.databinding.LayoutFileConveyingBinding;
import com.merlin.conveyor.ConveyGroup;
import com.merlin.conveyor.UploadConvey;
import com.merlin.dialog.FileConveyDialog;
import com.merlin.model.Model;
import com.merlin.view.OnTapClick;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class WebsiteModel  extends Model implements Label, OnTapClick, Model.OnActivityResult {
//    public static final String mUrl="http://192.168.0.6:5005";
    public static final String SERVER_IP="http://192.168.0.2";
    public static final int SERVER_PORT=45678;
    public static final String mUrl=SERVER_IP+":"+SERVER_PORT;
//    public static final String mUrl="http://172.16.20.212:45678";
    private final static int PHOTO_CHOOSE_REQUEST_CODE=234234;

    private interface Api{
        @POST("/travel/category")
        @FormUrlEncoded
        Observable<Reply<PageData<TravelCategory>>> getCategories(@Field(LABEL_WHAT) String type,@Field(LABEL_NAME) String name, @Field(LABEL_FROM) int from, @Field(LABEL_TO) int to);

        @POST("/travel/category/create")
        @FormUrlEncoded
        Observable<Reply<TravelCategory>> createCategory(@Field(LABEL_ID) Long id,@Field(LABEL_TITLE) String title,
                                                         @Field(LABEL_BANNER) boolean banner,@Field(LABEL_NOTE) String note,
                                                         @Field(LABEL_URL) Integer coverId);
    }

    private ObservableField<RecyclerView.Adapter> mAdapter=new ObservableField<>();
    private final WebsiteBannerAdapter mImagesAdapter=new WebsiteBannerAdapter(){
        @Override
        protected Canceler onPageLoad(String arg, int from, OnApiFinish<Reply<PageData<IPath>>> finish) {
            return call(prepare(Api.class,mUrl).getCategories(null,arg,from,from+10),finish);
        }
    };

    private final WebsiteCategoriesAdapter mCategoriesAdapter=new WebsiteCategoriesAdapter(){
        @Override
        protected Canceler onPageLoad(String arg, int from, OnApiFinish<Reply<PageData<TravelCategory>>> finish) {
            return call(prepare(Api.class,mUrl).getCategories(Label.LABEL_BANNER,arg,from,from+10),finish);
        }
    };

    @Override
    protected void onRootAttached(View root) {
        super.onRootAttached(root);
        startActivity(WebsiteTravelCategoryActivity.class);
        finishActivity(null);
    }

    @Override
    public boolean onTapClick(View view, int clickCount, int resId, Object data) {
        switch (resId){
            case R.string.add:
                startActivity(new Intent(view.getContext(), LocalPhotoChooseActivity.class),PHOTO_CHOOSE_REQUEST_CODE);
//                createCategory();
                break;
            default:
                if (null!=data&&data instanceof TravelCategory){

                }
        }
        return true;
    }

    private boolean queryCategories(){
        WebsiteCategoriesAdapter categoriesAdapter=mCategoriesAdapter;
        return null!=categoriesAdapter&&categoriesAdapter.loadPage("","");
    }

    public boolean queryImages(){
        WebsiteBannerAdapter bannerAdapter=mImagesAdapter;
        return null!=bannerAdapter&&bannerAdapter.loadPage("","");
    }

    public boolean createCategory(){
//        Dialog dialog=new Dialog(getViewContext());
        call(prepare(Api.class,mUrl).createCategory(0l,"林强37",true,"Note", 1));
//        return dialog.show();
        return false;
    }

    public ObservableField<RecyclerView.Adapter> getAdapter() {
        return mAdapter;
    }

    private boolean uploadFiles(List<IPath> paths){
        if (null==paths||paths.size()<=0){
            return toast(R.string.listEmpty)&&false;
        }
        UploadConvey convey=null;
        String folder="lovePhotos";
        ConveyGroup<UploadConvey> group=new ConveyGroup<>();
        boolean empty=true;
//        Path remoteFolder=new Path(mUrl,folder,null,null);
        for (IPath child:paths) {
            String path=null!=child?child.getPath():null;
//            if (null!=(convey=null!=path&&path.length()>0? new UploadConvey(Path.build(path,null),
//                    remoteFolder, CoverMode.SKIP):null)&&group.add(convey)){
//                empty=false;
//            }
        }
        if (empty){
            return toast(R.string.listEmpty)&&false;
        }
        final LayoutFileConveyingBinding binding=inflate(R.layout.layout_file_conveying);
        final FileConveyDialog dialog=new FileConveyDialog(binding);
        dialog.convey(this,group,"");
        return dialog.title(R.string.upload).show();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PHOTO_CHOOSE_REQUEST_CODE:
                Bundle bundle=null!=data?data.getExtras():null;
                Object object=null!=bundle?bundle.get(Label.LABEL_DATA):null;
                if (null!=object&&object instanceof ArrayList) {
                    List<IPath> paths=new ArrayList<>();
                    for (Object child : (ArrayList) object) {
                        if (null != child&&child instanceof IPath) {
                            paths.add((IPath)child);
                        }
                    }
                    uploadFiles(paths);
                }
                break;
        }
    }
}
