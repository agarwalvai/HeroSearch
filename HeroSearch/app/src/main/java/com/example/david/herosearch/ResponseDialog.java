package com.example.david.herosearch;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ResponseDialog extends DialogFragment {

    static responseDialogListener mListener;

    static ResponseDialog newInstance(responseDialogListener listener)
    {
        mListener = listener;
        return new ResponseDialog();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.response_dialog, container, false);

        final EditText message_field = v.findViewById(R.id.response_dialog_message);
        final Button submit_button = v.findViewById(R.id.response_dialog_submit);

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = message_field.getText().toString();

                mListener.onResponseDialogSubmit(message);
            }
        });

        return v;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setTitle("Confirm Response");

        return dialog;
    }
}
