package com.starrysky.nextor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class EditorFragment extends Fragment implements TextWatcher {

    private EditText editText;
    private String filename;
    private File file;
    private boolean flag = false;
    private boolean flag_stack = true;

    private final Stack<String> uStack = new Stack<>();
    private final Stack<String> rStack = new Stack<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        editText = getView().findViewById(R.id.editText);
        editText.addTextChangedListener(this);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                editText.setSelection(editText.getText().length());
                InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (flag_stack) {
            uStack.push(editText.getText().toString());
            rStack.clear();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (flag) {
            if (filename.charAt(0) != '*') {
                filename = "*" + filename;
                ((MainActivity) getActivity()).setName(filename);
            }
        } else {
            flag = true;
        }
    }

    public void undo() {
        if (!uStack.empty()) {
            String str = uStack.pop();
            rStack.push(editText.getText().toString());
            flag_stack = false;
            editText.setText(str);
            editText.setSelection(editText.getText().length());
            flag_stack = true;
        }
    }

    public void redo() {
        if (!rStack.empty()) {
            String str = rStack.pop();
            uStack.push(editText.getText().toString());
            flag_stack = false;
            editText.setText(str);
            editText.setSelection(editText.getText().length());
            flag_stack = true;
        }
    }

    public void read() {
        if (file != null) {
            try {
                String str = FileService.read(file);
                flag_stack = false;
                setText(str);
                editText.setSelection(editText.getText().length());
                flag_stack = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write() {
        if (file != null) {
            FileService.write(getText(), file);
            if (filename.charAt(0) == '*') {
                filename = filename.substring(1);
            }
            ((MainActivity) getActivity()).setName(filename);
        }
    }

    public String getText() {
        return String.valueOf(editText.getText());
    }

    public void setText(String str) {
        editText.setText(str);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void selectAll() {
        editText.selectAll();
    }

    public void copyAndCut(String mode) {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        String str = editText.getText().toString();
        String txt = str.substring(selectionStart, selectionEnd);
        String str1 = str.substring(0, selectionStart);
        String str2 = str.substring(selectionEnd);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, txt));
        if (mode.equals("cut")) {
            editText.setText(str1 + str2);
        }
    }

    public void paste() {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            CharSequence text = clipData.getItemAt(0).getText();
            editText.setText(text);
        }
    }

    public EditText getEditText() {
        return editText;
    }
}
