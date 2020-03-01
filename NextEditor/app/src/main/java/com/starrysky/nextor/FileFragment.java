package com.starrysky.nextor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileFragment extends Fragment {

    private TextView tv_path;
    private FloatingActionButton fab,new_folder,save_as;
    private boolean flag = true;
    private FileAdapter adapter;
    private List<File> list = new ArrayList<>();
    private File file;
    private boolean operation = true;
    private EditorFragment editorFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    backLast();
                    return true;
                }
                return false;
            }
        });
    }

    private void initView(){
        file = new File(FileService.getStoragePath(getContext(),false));
        RecyclerView recyclerView = getActivity().findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        tv_path = getActivity().findViewById(R.id.path);
        adapter = new FileAdapter(list,this);
        recyclerView.setAdapter(adapter);
        load(file);

        fab = getView().findViewById(R.id.fab);
        new_folder = getView().findViewById(R.id.new_folder);
        save_as = getView().findViewById(R.id.save_as);
        new_folder.hide();
        save_as.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    if (editorFragment != null && !operation){
                        save_as.show();
                        save_as.animate().translationY(-400).start();
                    }
                    new_folder.show();
                    new_folder.animate().translationY(-200).start();
                    flag = false;
                } else {
                    new_folder.hide();
                    save_as.hide();
                    flag = true;
                }
            }
        });
        new_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFolder(file);
                new_folder.hide();
                save_as.hide();
                flag = true;
            }
        });
        save_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAs(file);
                new_folder.hide();
                save_as.hide();
                flag = true;
            }
        });
    }

    public void load(File file){
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            list.clear();
            Collections.addAll(list, files);
            Collections.sort(list);
            tv_path.setText(file.getPath());
            adapter.notifyDataSetChanged();
        } else if (operation){
            ((MainActivity)getActivity()).openFile(file);
            getActivity().onBackPressed();
        }
    }

    private void newFolder(final File file){
        final String path = file.getPath();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_text, null);
        final EditText editText = view.findViewById(R.id.edit_text);
        new AlertDialog.Builder(getActivity())
                .setTitle("新建文件夹")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       File file = new File(path + "/" + editText.getText());
                        if (!file.exists()){
                            file.mkdir();
                            list.add(file);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(),"创建成功",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(),"创建失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消",null).show();
    }

    private void saveAs(final File file){
        final String path = file.getPath();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_text, null);
        final EditText editText = view.findViewById(R.id.edit_text);
        new AlertDialog.Builder(getActivity())
                .setTitle("保存为")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(path + "/" + editText.getText());
                        if (!file.exists()){
                            try {
                                file.createNewFile();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            if (editorFragment.getFile() != null){
                                    String value = editorFragment.getText();
                                    FileService.write(value,file);
                                    editorFragment.setFile(file);
                                    editorFragment.setFilename(String.valueOf(editText.getText()));
                            } else {
                                editorFragment.setFile(file);
                                editorFragment.setFilename(String.valueOf(editText.getText()));
                                editorFragment.write();
                                ((MainActivity) getActivity()).saveAs(file);
                            }
                            list.add(file);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("文件已存在，是否覆盖文件")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            File file = new File(path + "/" + editText.getText());
                                            try {
                                                file.createNewFile();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (editorFragment.getFile() != null){
                                                String value = editorFragment.getText();
                                                FileService.write(value,file);
                                                editorFragment.setFile(file);
                                                editorFragment.setFilename(String.valueOf(editText.getText()));
                                            } else {
                                                editorFragment.setFile(file);
                                                editorFragment.setFilename(String.valueOf(editText.getText()));
                                                editorFragment.write();
                                                ((MainActivity) getActivity()).saveAs(file);
                                            }
                                            list.add(file);
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
                                            getActivity().onBackPressed();
                                        }
                                    }).setNegativeButton("取消",null).show();

                        }
                    }
                }).setNegativeButton("取消",null).show();
    }

    public void backLast(){
        try{
            load(new File(file.getParent()));
            file = new File(file.getParent());
        } catch (Exception e){
            getActivity().onBackPressed();
        }
    }

    public void setOperation(boolean operation) {
        this.operation = operation;
    }

    public void setEditorFragment(EditorFragment editorFragment) {
        this.editorFragment = editorFragment;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
