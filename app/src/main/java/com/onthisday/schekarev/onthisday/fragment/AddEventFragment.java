package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.model.Event;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.onthisday.schekarev.onthisday.utils.Utils;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.onthisday.schekarev.onthisday.utils.Const.Extra.EXTRA_EVENT_DATE;

/**
 * Фрагмент добавления события в БД.
 */
public class AddEventFragment extends Fragment implements View.OnClickListener {
    private MaterialEditText mTitleEditText;
    private MaterialEditText mDescriptionEditText;
    private ImageView mImageView;
    private String mImageName;
    private Event mEvent;
    private Date mDate;
    private Intent intent;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mEvent = new Event();
        mDate = (Date) Objects.requireNonNull(getActivity()).getIntent().getSerializableExtra(EXTRA_EVENT_DATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_add_event, null);

        TextView dateTextView = view.findViewById(R.id.event_date_text_view);
        dateTextView.setText(DateFormat.format(Const.Date.DATE_TIME_FORMAT, mDate));
        mTitleEditText = view.findViewById(R.id.title_field);
        mDescriptionEditText = view.findViewById(R.id.description_field);
        mImageView = view.findViewById(R.id.event_photo);
        ImageButton chooseButton = view.findViewById(R.id.event_image);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        mEvent.setRateEvent(2);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.event_excellent: // кнопка «Отлично».
                        mEvent.setRateEvent(3);
                        break;
                    case R.id.event_typical: // кнопка «Обычно».
                        mEvent.setRateEvent(2);
                        break;
                    case R.id.event_bad: // кнопка «Плохо».
                        mEvent.setRateEvent(1);
                        break;
                }
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference(Const.DB.DB_EVENTS);
        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_EVENT_IMAGES);

        chooseButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.event_image) {
            chooseImage();
        }
    }

    /**
     * Выбор фото из галереи в локальном хранилище.
     */
    private void chooseImage() {
        intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Const.Request.REQUEST_IMAGE);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveEvent(final String userId, final String title, final String description,
                           final int rateEvent, final Date date) {
        String key = mDatabase.push().getKey();

        if (intent != null)
            mImageName = UUID.randomUUID().toString();

        Event event = new Event(key, userId, title, description, rateEvent, date, mImageName);
        event.setKey(key);
        event.setUserUID(userId);
        event.setTitle(title);
        event.setDescription(description);
        event.setRateEvent(rateEvent);
        event.setDate(date);
        event.setImage(mImageName);

        Map<String, Object> eventValues = event.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, eventValues);
        mDatabase.updateChildren(childUpdates);

        if (mImageName != null)
            Utils.ImageOut.uploadImage(mStorage, mImageView, mImageName, 40, 2);
    }

    /**
     * Диалоговое окно ProgressBar.
     */
    private void dialogLoading() {
        FragmentManager manager = getFragmentManager();
        LoadingFragment dialog = new LoadingFragment();
        dialog.show(Objects.requireNonNull(manager), Const.Dialog.DIALOG_LOADING);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_add_event_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save_event) {
            dialogLoading();
            String userId = FirebaseAuth.getInstance().getUid();
            saveEvent(userId, Objects.requireNonNull(mTitleEditText.getText()).toString(),
                    Objects.requireNonNull(mDescriptionEditText.getText()).toString(), mEvent.getRateEvent(), mDate);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Objects.requireNonNull(getActivity()).finish();
                }
            }, 1000);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
