package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.utils.Const;

import java.util.Objects;

import static com.onthisday.schekarev.onthisday.utils.Const.Argument.ARG_EVENT;
import static com.onthisday.schekarev.onthisday.utils.Const.Argument.ARG_IMAGE;
import static com.onthisday.schekarev.onthisday.utils.Const.DB.DB_EVENTS;
import static com.onthisday.schekarev.onthisday.utils.Const.Dialog.DIALOG_EDIT;

public class OptionsEventFragment extends DialogFragment implements View.OnClickListener {

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private static String sKey;
    private static String sImageName;

    public static OptionsEventFragment newInstance(String key, String imageName) {
        sKey = key;
        sImageName = imageName;

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, key);
        args.putSerializable(ARG_IMAGE, imageName);

        OptionsEventFragment fragment = new OptionsEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_options, null);

        TextView editTextView = view.findViewById(R.id.edit_event_text_view);
        TextView deleteTextView = view.findViewById(R.id.delete_event_text_view);

        mDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS);
        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_EVENT_IMAGES);

        editTextView.setOnClickListener(this);
        deleteTextView.setOnClickListener(this);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_event_text_view:
                editEvent();
                Objects.requireNonNull(getDialog()).dismiss();
                break;
            case R.id.delete_event_text_view:
                deleteEvent();
                Objects.requireNonNull(getDialog()).dismiss();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    private void editEvent() {
        FragmentManager manager = getFragmentManager();
        EditEventFragment dialog = EditEventFragment.newInstance(sKey);
        dialog.show(Objects.requireNonNull(manager), DIALOG_EDIT);
    }

    private void deleteEvent() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle(R.string.delete_event)
                .setMessage(R.string.warning_message)
                .setCancelable(true)
                .setIcon(R.drawable.ic_delete_forever)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.delete_event, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child(sKey).removeValue();
                        StorageReference ref = mStorage.child(sImageName + ".jpg");
                        ref.delete();
                        dialog.dismiss();
                    }
                });
        androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
