package com.pangbai.dowork.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pangbai.dowork.PropertiesActivity;
import com.pangbai.dowork.R;
import com.pangbai.dowork.databinding.FragmentContainerBinding;
import com.pangbai.dowork.tool.containerInfor;
import com.pangbai.dowork.tool.ctAdapter;
import com.pangbai.dowork.tool.uiThreadUtil;
import com.pangbai.dowork.tool.util;
import com.pangbai.linuxdeploy.PrefStore;
import com.pangbai.view.dialogUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class containerFragment extends Fragment implements View.OnClickListener{


    ctAdapter.OnItemChange mOnItemChange=new ctAdapter.OnItemChange(){
        @Override
        public void OnItemChange(containerInfor infor) {
            binding.ctMethod.setText(infor.method.toUpperCase());
            currentContainer=infor;
        }};
    FragmentContainerBinding binding;
    ctAdapter adapter;
    containerInfor currentContainer;
    private ExecutorService  executorService = Executors.newFixedThreadPool(1);;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // binding = FragmentContainerBinding.inflate(inflater);



        binding.ctAdd.setOnClickListener(this);
        binding.ctDelete.setOnClickListener(this);
        binding.ctRename.setOnClickListener(this);
        executorService = Executors.newFixedThreadPool(1);
       // doInBackground();


        return binding.getRoot();
    }



    @Override
    public void onClick(View view) {
        if (view == binding.ctAdd) {
             dialogUtils.showInputDialog(getContext(),
                    "创建容器",
                    (dialogUtils.DialogInputListener) userInput -> {
                        if (containerInfor.getContainerByName(userInput)!=null){
                            Toast.makeText(getActivity(),"容器已存在",Toast.LENGTH_LONG).show();
                            return;}
                        if(userInput==null)
                            return ;
                        PrefStore.changeProfile(getContext(),userInput);
                        util.startActivity(getActivity(), PropertiesActivity.class, false);
            });
            
        }else if (view==binding.ctDelete){
             int containerSize= containerInfor.ctList.size();
            if (containerSize==1){
                Toast.makeText(getContext(),"请确保至少一个容器",Toast.LENGTH_SHORT).show();
                return;}
            if (currentContainer!=null)
                    dialogUtils.showConfirmationDialog(getContext(),
                            "删除容器",
                            "确定要删除"+currentContainer.name+"吗？你将会丢失容器的数据。",
                            "确认删除",
                            "取消",
                            () -> {
                                Dialog mdialog= dialogUtils.showCustomLayoutDialog(getContext(),"删除容器"+currentContainer.name, R.layout.dialog_loading);
                                new Thread(){
                                    @Override
                                    public void run() {
                                    boolean result= containerInfor.reMoveContainer(currentContainer);
                                     mdialog.dismiss();
                                     if(!result)
                                         return;
                                     if (--ctAdapter.selectedPosition<0)
                                        ctAdapter.selectedPosition=0;
                                    containerInfor.ctList.remove(currentContainer);
                                    currentContainer= containerInfor.ctList.get(ctAdapter.selectedPosition);
                                    PrefStore.changeProfile(getContext(),currentContainer.name);
                                        uiThreadUtil.runOnUiThread(() ->{ adapter.notifyDataSetChanged();});
                                    }}.start();},
                            null);

        }else if (view==binding.ctRename){
            dialogUtils.showInputDialog(getContext(),
                    "重命名容器",
                    (dialogUtils.DialogInputListener) userInput -> {
                        if (containerInfor.getContainerByName(userInput)!=null){
                            Toast.makeText(getActivity(),"重命名失败,容器已存在",Toast.LENGTH_LONG).show();
                            return;}
                        File oldFile = PrefStore.getPropertiesConfFile(getContext());
                        File newFile = new File(PrefStore.getEnvDir(getContext()) + "/config/" + userInput + ".conf");
                        oldFile.renameTo(newFile);
                        currentContainer.name=userInput;
                        adapter.notifyDataSetChanged();
                        PrefStore.changeProfile(getContext(),userInput);
            });


        }
    }


    @Override
    public void onResume() {
       // doInBackground();
        super.onResume();
        doInBackground();
    }



    public void doInBackground( ){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<String> ctName = containerInfor.getProfiles(getContext());
                List ctList= containerInfor.setInforList(ctName);
                
                uiThreadUtil.runOnUiThread(() -> {
                    adapter.setData(ctList);
                  //  adapter.notifyLastItem();
                   // adapter.notifyDataSetChanged();
                    binding.ctBar.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                });

            }});}



    @Override
    public void onDestroyView() {
        if (!executorService.isShutdown())
            executorService.shutdownNow();
        //adapter.setData(null);
        binding.ctDelete.setOnClickListener(null);
        binding.ctRename.setOnClickListener(null);
        binding.ctAdd.setOnClickListener(null);
        //adapter.ItemChange=null;
       // adapter=null;
        super.onDestroyView();
       // binding=null;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    binding = FragmentContainerBinding.inflate(getLayoutInflater());
        adapter = new ctAdapter(containerInfor.ctList,mOnItemChange);
        binding.ctList.setAdapter(adapter);
        binding.ctList.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
