package com.example.android.shushme;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

public class ListItemFragment extends DialogFragment {
    private static final String TAG = ListItemFragment.class.getSimpleName();
    private boolean mDeleteChecked = false;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Log.i(TAG, "empty bundle!!!");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_listitem, null);
        final EditText mRadius = (EditText) mView.findViewById(R.id.radius);
        //mRadius.setText(String.valueOf(savedInstanceState.getFloat("radius", 100)));
        mRadius.setText(String.valueOf(100));
        final EditText mUpdate = (EditText) mView.findViewById(R.id.location_updates);
        // mUpdate.setText(String.valueOf(savedInstanceState.getInt("update", 300)));
        mUpdate.setText(String.valueOf(300));
        final CheckBox mDelete = (CheckBox) mView.findViewById(R.id.delete_location_checkbox);
        builder.setView(mView)
                // Add action buttons
        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.i(TAG, "Clicked save");
                dialog.dismiss();
                String s = mRadius.getText().toString();
                float rad = s.isEmpty() ? -1 : Float.valueOf(s);
                s = mUpdate.getText().toString();
                Integer upd = s.isEmpty() ? -1 : Integer.valueOf(s);
                Boolean checked = mDelete.isChecked();
                Log.i(TAG, rad + "   " + upd + "   " + checked);
                listener.onDialogPositiveClick(rad, upd, checked);
            }
        })
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                  // ListItemFragment.this.getDialog().cancel();
                 listener.onDialogNegativeClick(ListItemFragment.this);
                 dialog.cancel();
              }
         });
        return builder.create();
    }

    /* the activity that creates an instance of this dialog frament must
     * implement this interface in order to receive event callbacksl
     * Each method passes the DialogFragment in case the host neds to query it.
     */
    public interface ListItemListener {
        public void onDialogPositiveClick(float radius, Integer updates, Boolean cheked);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to delvier action events
    ListItemListener listener;

    // Override the Fragment.onAttach() method to instantiate the ListItemListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that MainActivity implements the callback interface
        try {
            // Instantiate the ddialog listener
            listener = (ListItemListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ListItemListener");
        }
    }

}
