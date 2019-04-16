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

public class ListItemFragment extends DialogFragment {
    private static final String TAG = ListItemFragment.class.getSimpleName();
    private boolean mDeleteChecked = false;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Inflate the dialog and set the layout
        builder.setView(inflater.inflate(R.layout.dialog_listitem, null))
                // Add action buttons
        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.i(TAG, "Clicked save");
                listener.onDialogPositiveClick(ListItemFragment.this);
            }
        })
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                  // ListItemFragment.this.getDialog().cancel();
                 listener.onDialogNegativeClick(ListItemFragment.this);
              }
         });
        return builder.create();
    }

    /* the activity that creates an instance of this dialog frament must
     * implement this interface in order to receive event callbacksl
     * Each method passes the DialogFragment in case the host neds to query it.
     */
    public interface ListItemListener {
        public void onDialogPositiveClick(DialogFragment dialog);
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

    public void onDeleteBoxChecked(View v) {
        Log.i(TAG, "Delete box checked, initial value: " + mDeleteChecked);
        mDeleteChecked = !mDeleteChecked;
        Log.i(TAG, "Final value: " + mDeleteChecked);
    }

}
