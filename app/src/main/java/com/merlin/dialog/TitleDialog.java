package com.merlin.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.merlin.client.R;
import com.merlin.client.databinding.DlgLayoutBinding;
import com.merlin.model.BaseModel;
import com.merlin.util.Text;

public class TitleDialog extends Dialog implements BaseModel.OnModelViewClick {

    protected Integer onResolveContentLayout(){
        //Do nothing
        return null;
    }

    public TitleDialog(Context context){
        super(context);
        setContentView(R.layout.dlg_layout);
        ViewGroup vg=findViewById(R.id.dlg_contentFL,ViewGroup.class);
        Integer layoutId=null!=vg?onResolveContentLayout():null;
        if (null!=layoutId){
            LayoutInflater.from(context).inflate(layoutId,vg);
        }
        final View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(v,v.getId());
            }
        };
        findViewById(R.id.dlg_sureTV,View.class).setOnClickListener(listener);
        findViewById(R.id.dlg_cancelTV,View.class).setOnClickListener(listener);
    }

    public void setTitle(Integer titleId){
        if (null!=titleId){
            String text=Text.text(getContext(),null,titleId);
            TextView tv=findViewById(R.id.dlg_titleTV, TextView.class);
            if (null!=tv){
                tv.setVisibility(null!=text?View.VISIBLE:View.GONE);
                tv.setText(null!=text?text:"");
            }
        }
    }

    public boolean show(Integer titleId){
        if (null!=titleId){
            if (!super.isShowing()&&null!=super.show()&&super.isShowing()){
                 setTitle(titleId);
                 return true;
            }
        }
        return false;
    }

    @Override
    public void onViewClick(View v, int id) {

    }
}
