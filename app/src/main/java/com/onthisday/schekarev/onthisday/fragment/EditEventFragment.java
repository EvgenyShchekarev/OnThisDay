package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.model.Event;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.onthisday.schekarev.onthisday.utils.Utils;

import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.onthisday.schekarev.onthisday.utils.Const.Argument.ARG_EVENT;

public class EditEventFragment extends DialogFragment {

    private TextView mDateTextView;
    private EditText mTitleEditText;
    private ImageView mImageView;
    private EditText mDescriptionEditText;
    private RadioButton mExcellentRadioButton;
    private RadioButton mTypicalRadioButton;
    private RadioButton mBadRadioButton;
    private RadioGroup mRateEventRadioGroup;

    private Event mEvent;
    private String mKey;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    public static EditEventFragment newInstance(String key) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, key);

        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_event, null);
        mDateTextView = view.findViewById(R.id.edit_event_year_text_view);
        mTitleEditText = view.findViewById(R.id.edit_event_title_field);
        mImageView = view.findViewById(R.id.edit_event_image);
        mDescriptionEditText = view.findViewById(R.id.edit_event_description_field);
        mExcellentRadioButton = view.findViewById(R.id.edit_event_excellent);
        mTypicalRadioButton = view.findViewById(R.id.edit_event_typical);
        mBadRadioButton = view.findViewById(R.id.edit_event_bad);
        mRateEventRadioGroup = view.findViewById(R.id.edit_event_radio_group);

        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_EVENT_IMAGES);
        mDatabase = FirebaseDatabase.getInstance().getReference(Const.DB.DB_EVENTS);
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (getArguments() != null) {
                    mKey = getArguments().getString(ARG_EVENT);
                    if (Objects.requireNonNull(snapshot.getKey()).equals(mKey)) {
                        mEvent = snapshot.getValue(Event.class);
                        updateUI(Objects.requireNonNull(mEvent));
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateEvent();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void updateEvent() {
        mEvent.setTitle(mTitleEditText.getText().toString());
        mEvent.setDescription(mDescriptionEditText.getText().toString());
        mDatabase.child(mKey).setValue(mEvent);
    }

    private void updateUI(Event event) {
        mDateTextView.setText(DateFormat.format(Const.Date.DATE_TIME_FORMAT, event.getDate()));
        mTitleEditText.setText(event.getTitle());
        mDescriptionEditText.setText(event.getDescription());
        switch (mEvent.getRateEvent()) {
            case 3:
                mExcellentRadioButton.setChecked(true);
                break;
            case 2:
                mTypicalRadioButton.setChecked(true);
                break;
            case 1:
                mBadRadioButton.setChecked(true);
                break;
        }
        mRateEventRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.edit_event_excellent: // кнопка «Отлично».
                        mEvent.setRateEvent(3);
                        break;
                    case R.id.edit_event_typical: // кнопка «Обычно».
                        mEvent.setRateEvent(2);
                        break;
                    case R.id.edit_event_bad: // кнопка «Плохо».
                        mEvent.setRateEvent(1);
                        break;
                }
            }
        });
        if (mEvent.getImage() != null)
            Utils.ImageIn.showImageView(mStorage, mImageView, mEvent.getImage());
        else mImageView.setImageDrawable(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Const.Request.REQUEST_IMAGE &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri photoPath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), photoPath);
                mImageView.setImageBitmap(bitmap);
                if (mEvent.getImage() != null) {
                    Utils.ImageOut.uploadImage(mStorage, mImageView, mEvent.getImage(), 40, 2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
