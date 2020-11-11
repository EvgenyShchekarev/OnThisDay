package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import static com.onthisday.schekarev.onthisday.utils.Const.Dialog.DIALOG_EDIT;

/**
 * Фрагмент события.
 */
public class EventFragment extends Fragment {
    private TextView mDateTextView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private ImageView mImageView;
    private ImageView mRateEventImageView;
    private Event mEvent;
    private String eventKey;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        eventKey = Objects.requireNonNull(getActivity()).getIntent().getStringExtra(Const.Extra.EXTRA_EVENT_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEvent();
        updateUI();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        mDateTextView = view.findViewById(R.id.event_date_text_view);
        mTitleTextView = view.findViewById(R.id.title_text_view);
        mDescriptionTextView = view.findViewById(R.id.description_text_view);
        mImageView = view.findViewById(R.id.image_view);
        mRateEventImageView = view.findViewById(R.id.event_rating);
        ImageButton shareEvent = view.findViewById(R.id.event_report);
        shareEvent.setOnClickListener(new View.OnClickListener() {
            /**
             * Отправляем событие по cmc, на почту или в соц.сети.
             * @param v - кнопка
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.event_report,
                                mEvent.getTitle(),
                                mEvent.getDescription(),
                                DateFormat.format(Const.Date.DATE_FORMAT, mEvent.getDate())));
                intent = Intent.createChooser(intent, getString(R.string.send_event_report));
                startActivity(intent);
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference(Const.DB.DB_EVENTS);
        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_EVENT_IMAGES);
        updateUI();

        return view;
    }

    private void updateUI() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (Objects.requireNonNull(snapshot.getKey()).equals(eventKey)) {
                    mEvent = snapshot.getValue(Event.class);
                    mDateTextView.setText(DateFormat.format(
                            Const.Date.DATE_TIME_FORMAT, Objects.requireNonNull(snapshot.getValue(Event.class)).getDate()));
                    mTitleTextView.setText(Objects.requireNonNull(snapshot.getValue(Event.class)).getTitle());
                    mDescriptionTextView.setText(Objects.requireNonNull(snapshot.getValue(Event.class)).getDescription());
                    switch (mEvent.getRateEvent()) {
                        case 3:
                            mRateEventImageView.setImageResource(R.drawable.three_stars);
                            break;
                        case 2:
                            mRateEventImageView.setImageResource(R.drawable.two_stars);
                            break;
                        case 1:
                            mRateEventImageView.setImageResource(R.drawable.one_star);
                            break;
                    }
                    if (mEvent.getImage() != null) {
                        Utils.ImageIn.showImageView(mStorage, mImageView, mEvent.getImage());
                        mImageView.setPadding(0, 20, 0, 30);
                    } else {
                        mImageView.setImageDrawable(null);
                        mImageView.setPadding(0, 0, 0, 5);
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
    }

    /**
     * Обновляем событие.
     */
    private void updateEvent() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals(mEvent.getKey())) {
                    mDatabase.child(snapshot.getKey()).setValue(mEvent);
                    updateUI();
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Const.Request.REQUEST_IMAGE &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri photoPath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity())
                                .getContentResolver(), photoPath);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Диалоговое окно редактирования события.
     */
    private void editEventDialog() {
        FragmentManager manager = getFragmentManager();
        EditEventFragment dialog = EditEventFragment.newInstance(mEvent.getKey());
        dialog.show(Objects.requireNonNull(manager), DIALOG_EDIT);
    }

    /**
     * Диалоговое окно удаления события.
     */
    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
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
                        mDatabase.child(mEvent.getKey()).removeValue();
                        StorageReference ref = mStorage.child(mEvent.getImage() + ".jpg");
                        ref.delete();
                        Objects.requireNonNull(getActivity()).finish();
                        toastMessage();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void toastMessage() {
        Toast.makeText(getActivity(), R.string.event_deletion_completed, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_detail, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_event:
                deleteDialog();
                return true;
            case R.id.edit_event:
                editEventDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}