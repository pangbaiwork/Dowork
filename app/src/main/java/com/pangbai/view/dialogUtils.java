package com.pangbai.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.pangbai.dowork.R;

public class dialogUtils {

    public interface OnPositiveClickListener {
        void onPositiveButtonClick();
    }
    public interface OnNegativeClickListener {
        void onNegativeButtonClick();
    }
    public interface DialogInputListener {
        void onConfirm(String userInput);
    }


    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveButtonText, String negativeButtonText,
                                              final OnPositiveClickListener positiveClickListener,final OnNegativeClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (positiveClickListener != null) {
                            positiveClickListener.onPositiveButtonClick();
                        }
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (negativeClickListener != null) {
                            negativeClickListener.onNegativeButtonClick();
                        }
                      //  dialogInterface.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static Dialog showCustomLayoutDialog(Context context,String title, int layoutResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate custom layout
        View view=LayoutInflater.from(context).inflate(layoutResId,null);
            TextView text= view.findViewById(R.id.dialog_title);
        if (title!=null&&text!=null)
            text.setText(title);

        builder.setView(view);


        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false); // Prevent dismiss on outside touch

        alertDialog.show();
        return alertDialog;
    }

    public static void showInputDialog(Context context, String title, final DialogInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_input, null);

        EditText inputEditText = dialogView.findViewById(R.id.dialog_text_input);

        builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = inputEditText.getText().toString();
                        if (listener != null) {
                            listener.onConfirm(userInput);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        inputEditText.requestFocus();
    }


}