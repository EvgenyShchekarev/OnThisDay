package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.EventListActivity;
import com.onthisday.schekarev.onthisday.activity.ProfileActivity;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.activity.StartActivity;
import com.onthisday.schekarev.onthisday.model.Event;
import com.onthisday.schekarev.onthisday.model.User;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.onthisday.schekarev.onthisday.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.onthisday.schekarev.onthisday.utils.Const.Tag.TAG_MAIN;

/**
 * Главная страница приложения.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private ConstraintLayout mRoot;
    private Button mShowEventButton;
    private TextView mCountEventTextView;
    private TextView mDayOfWeekTextView;
    private TextView mTodayTextView;
    private TextView mMonthTextView;
    private TextView mYearTextView;
    private TextView mNameTextView;
    private TextView mLogoutTextView;
    private CircleImageView mAvatarImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseEvents;
    private StorageReference mStorage;

    private User mUser;
    private List<Event> mEvents;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserFromDB();
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRoot = view.findViewById(R.id.root_element);
        mDayOfWeekTextView = view.findViewById(R.id.day_of_week_text_view);
        mTodayTextView = view.findViewById(R.id.today_text_view);
        mMonthTextView = view.findViewById(R.id.month_text_view);
        mYearTextView = view.findViewById(R.id.year_text_view);
        mAvatarImageView = view.findViewById(R.id.profile_image_button);
        mNameTextView = view.findViewById(R.id.name_text_view);
        mShowEventButton = view.findViewById(R.id.show_events_button);
        mLogoutTextView = view.findViewById(R.id.logout_text_view);
        mCountEventTextView = view.findViewById(R.id.count_event_text_view);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference(Const.DB.DB_USERS);
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference(Const.DB.DB_EVENTS);
        mStorage = FirebaseStorage.getInstance().getReference(
                Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_AVATARS_IMAGES);

        getUserFromDB();

        return view;
    }

    private void getUserFromDB() {
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUser = new User();
                mUser.setName(Objects.requireNonNull(snapshot.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).getValue(User.class)).getName());
                mUser.setEmail(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getEmail());
                mUser.setPass(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getPass());
                mUser.setBirthDate(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getBirthDate());
                mUser.setRegDate(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getRegDate());
                mUser.setAvatar(Objects.requireNonNull(snapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class)).getAvatar());
                getInitFields();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                snackBarMessage();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getInitFields() {
        mNameTextView.setText(getString(R.string.hello) + " " + mUser.getName());
        mDayOfWeekTextView.setText(DateFormat.format(Const.Date.DAY_OF_WEEK, new Date()));
        mTodayTextView.setText(DateFormat.format(Const.Date.DAY_INT, new Date()));
        mMonthTextView.setText(DateFormat.format(Const.Date.MONTH_STRING, new Date()));
        mYearTextView.setText(DateFormat.format(Const.Date.DATE_YEAR, new Date()));

        mEvents = new ArrayList<>();
        mDatabaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mEvents.clear();
                String mUserUID = FirebaseAuth.getInstance().getUid();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    assert mUserUID != null;
                    if (mUserUID.equals(Objects.requireNonNull(
                            dataSnapshot.getValue(Event.class)).getUserUID())) {
                        if (DateFormat.format(Const.Date.DATE_DAY_MONTH, new Date())
                                .equals(DateFormat.format(Const.Date.DATE_DAY_MONTH,
                                        Objects.requireNonNull(dataSnapshot.getValue(Event.class)).getDate()))) {
                            Event event = dataSnapshot.getValue(Event.class);
                            mEvents.add(event);
                        }
                    }
                }
                mCountEventTextView.setText(mEvents.size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG_MAIN, "onCancelled: " + error.getMessage());
            }
        });
        Utils.ImageIn.showImageView(mStorage, mAvatarImageView, mAuth.getUid());

        mShowEventButton.setOnClickListener(this);
        mLogoutTextView.setOnClickListener(this);
    }

    private void snackBarMessage() {
        Snackbar.make(mRoot, R.string.something_is_wrong, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_events_button:
                startActivity(new Intent(getActivity(), EventListActivity.class));
                break;
            case R.id.logout_text_view:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), StartActivity.class));
                Objects.requireNonNull(getActivity()).finish();
                toastMessage();
                break;
        }
    }

    private void toastMessage() {
        Toast.makeText(getActivity(), R.string.see_you_soon, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile_user) {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
