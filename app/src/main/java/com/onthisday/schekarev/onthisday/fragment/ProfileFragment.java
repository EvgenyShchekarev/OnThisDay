package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.model.User;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.onthisday.schekarev.onthisday.utils.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.onthisday.schekarev.onthisday.utils.Const.Tag.TAG_PROFILE;

/**
 * Фрагмент профиля пользователя.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout mRoot;
    private CircleImageView mAvatarImageView;
    private EditText mNameField;
    private EditText mEmailField;
    private Button mBirthDateButton;
    private User mUser;
    private Date mDate;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDate = new Date();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAvatarImageView = view.findViewById(R.id.profile_image_button);
        mNameField = view.findViewById(R.id.name_field);
        mEmailField = view.findViewById(R.id.email_field);
        mBirthDateButton = view.findViewById(R.id.birth_date_button);
        mRoot = view.findViewById(R.id.root_element);

        // Отключаем возможность редактировать поля.
        disableFields(false);

        mAvatarImageView.setOnClickListener(this);
        mBirthDateButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(Const.DB.DB_USERS);
        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_AVATARS_IMAGES);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Заполняем объект данными с БД.
                // Обновляем UI.
                getUserFromDB(snapshot);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG_PROFILE, error.toString());
            }
        });
        return view;
    }

    /**
     * Получение данных пользователя из БД.
     * Создание объекта и заполнение данными.
     * updateUI(User) - заполняет поля дынными объекта.
     *
     * @param snapshot - ссылка на объект в БД.
     */
    private void getUserFromDB(DataSnapshot snapshot) {
        mUser = new User();
        mUser.setName(Objects.requireNonNull(snapshot.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).getValue(User.class)).getName());
        mUser.setEmail(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getEmail());
        mUser.setPass(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getPass());
        mUser.setBirthDate(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getBirthDate());
        mUser.setRegDate(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getRegDate());
        mUser.setAvatar(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getAvatar());
    }

    /**
     * Управление полями.
     * Возможность включения и выключения редактирования данных полей.
     */
    private void disableFields(boolean value) {
        mAvatarImageView.setEnabled(value);
        mNameField.setFocusableInTouchMode(value);
        mEmailField.setFocusableInTouchMode(false);
        mNameField.setFocusable(value);
        mEmailField.setFocusable(false);
        mNameField.setClickable(value);
        mEmailField.setClickable(false);
        mBirthDateButton.setEnabled(value);
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        mNameField.setText(mUser.getName());
        mEmailField.setText(mUser.getEmail());
        mBirthDateButton.setText(getString(R.string.birthday) + " "
                + DateFormat.format(Const.Date.DATE_FORMAT, mUser.getBirthDate()));
        if (mUser.getAvatar() != null)
            Utils.ImageIn.showImageView(mStorage, mAvatarImageView, mAuth.getUid());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.birth_date_button:
                getDatePickerDialog();
                break;
            case R.id.profile_image_button:
                chooseImage();
                break;
        }
    }

    /**
     * Выбор фото из галереи в локальном хранилище.
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Const.Request.REQUEST_IMAGE);
    }

    /**
     * Запуск диологового окна с календарем.
     */
    private void getDatePickerDialog() {
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialogDate = DatePickerFragment.newInstance(new Date());
        dialogDate.setTargetFragment(ProfileFragment.this, Const.Request.REQUEST_DATE);
        assert manager != null;
        dialogDate.show(manager, Const.Dialog.DIALOG_DATE);
    }

    /**
     * Устанавливаем выбранную в календаре дату.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Const.Request.REQUEST_DATE &&
                resultCode == Activity.RESULT_OK && data != null) {
            mDate = (Date) data.getSerializableExtra(Const.Extra.EXTRA_EVENT_DATE);
            mUser.setBirthDate(mDate);
            mBirthDateButton.setText(getString(R.string.birthday) + " "
                    + DateFormat.format(Const.Date.DATE_FORMAT, mDate));
        } else if (requestCode == Const.Request.REQUEST_IMAGE &&
                data != null && data.getData() != null) {
            Uri imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath);
                mAvatarImageView.setImageBitmap(bitmap);
                Utils.ImageOut.uploadImage(mStorage, mAvatarImageView, mAuth.getUid(), 40, 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_edit_profile, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_event:
                disableFields(true);
                snackBarMessage(R.string.reade_to_edit);
                return true;
            case R.id.save_event:
                saveEvent();
                disableFields(false);
                snackBarMessage(R.string.update_successfully);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveEvent() {
        mUser.setName(mNameField.getText().toString());
        mUser.setEmail(mEmailField.getText().toString());
        mUser.setAvatar(Objects.requireNonNull(mAuth.getUid()));
        mDatabase.child(Objects.requireNonNull(mAuth.getUid())).setValue(mUser);
    }

    private void snackBarMessage(int message) {
        Snackbar.make(mRoot, message, Snackbar.LENGTH_SHORT).show();
    }
}
