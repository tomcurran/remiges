package org.tomcurran.remiges.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.tomcurran.remiges.R;

public class DeleteItemDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static DeleteItemDialogFragment newInstance(int messageId) {
        DeleteItemDialogFragment f = new DeleteItemDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE, messageId);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getArguments().getInt(ARG_MESSAGE))
                .setPositiveButton(R.string.dialog_item_delete_positive,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getTargetFragment().onActivityResult(getTargetRequestCode(), FragmentActivity.RESULT_OK, getActivity().getIntent());
                            }
                        }
                )
                .setNegativeButton(R.string.dialog_item_delete_negative,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getTargetFragment().onActivityResult(getTargetRequestCode(), FragmentActivity.RESULT_CANCELED, getActivity().getIntent());
                            }
                        }
                )
                .create();
    }

}
