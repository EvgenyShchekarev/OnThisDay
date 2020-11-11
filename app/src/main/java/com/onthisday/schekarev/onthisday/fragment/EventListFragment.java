package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onthisday.schekarev.onthisday.activity.AddEventActivity;
import com.onthisday.schekarev.onthisday.activity.EventPagerActivity;
import com.onthisday.schekarev.onthisday.activity.ProfileActivity;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.model.Event;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.onthisday.schekarev.onthisday.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.onthisday.schekarev.onthisday.utils.Const.Date.DATE_DAY_MONTH;
import static com.onthisday.schekarev.onthisday.utils.Const.Date.DATE_FORMAT;
import static com.onthisday.schekarev.onthisday.utils.Const.Dialog.DIALOG_DATE;
import static com.onthisday.schekarev.onthisday.utils.Const.Dialog.DIALOG_OPTIONS;
import static com.onthisday.schekarev.onthisday.utils.Const.Extra.EXTRA_EVENT_KEY;
import static com.onthisday.schekarev.onthisday.utils.Const.Request.REQUEST_DATE;
import static com.onthisday.schekarev.onthisday.utils.Const.Request.REQUEST_EVENT;
import static com.onthisday.schekarev.onthisday.utils.Const.Tag.TAG_EVENT_LIST;

/**
 * Фрагмент выводи списка RecyclerView с данными,
 * получеными с БД.
 */
public class EventListFragment extends Fragment {
    private TextView mEventsClear;
    private TextView mClearList;
    private TextView mDateTextView;
    private TextView mReadTextView;
    private ImageView mReadImageView;
    private ImageView mClearListImageView;
    private ImageView mAddEventImageButton;
    private RecyclerView mEventRecyclerView;
    private EventAdapter mAdapter;
    private Date mDate;
    private List<Event> mEvents;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDate = new Date();

        if (savedInstanceState != null) {
            mDate = (Date) savedInstanceState.getSerializable(Const.Key.KEY_INDEX);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        mDateTextView = view.findViewById(R.id.date_text_view);
        mEventsClear = view.findViewById(R.id.empty_events_text_view);
        mClearList = view.findViewById(R.id.clear_list_text_view);
        mClearListImageView = view.findViewById(R.id.clear_list_image_view);
        mAddEventImageButton = view.findViewById(R.id.add_event_image_button);
        mEventRecyclerView = view.findViewById(R.id.event_recycler_view);
        mDateTextView.setText(DateFormat.format(DATE_FORMAT, mDate));
        ImageButton calendarButton = view.findViewById(R.id.calendar_button);
        mEventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddEventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEvent();
            }
        });
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerDialog();
            }
        });
        updateUI();
        return view;
    }

    /**
     * Класс Adapter для RecyclerView.
     */
    private class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
        public EventAdapter() {}

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_day, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final EventViewHolder holder, int position) {
            holder.bind(mEvents.get(position));
        }

        @Override
        public int getItemCount() {
            updateSubtitle();
            return mEvents.size();
        }
    }

    /**
     * Класс ViewHolder заполнения данных для RecyclerView.
     */
    private class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Event mEvent;
        private final ImageView mImageView;
        private final ImageView mRateEventImageView;
        private final ImageView mOptionsImageView;
        private final TextView mDateTextView;
        private final TextView mTitleTextView;
        private final TextView mDescriptionTextView;
        private final StorageReference mStorage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            mDateTextView = itemView.findViewById(R.id.event_date_text_view);
            mTitleTextView = itemView.findViewById(R.id.title_text_view);
            mDescriptionTextView = itemView.findViewById(R.id.description_text_view);
            mImageView = itemView.findViewById(R.id.image_view);
            mRateEventImageView = itemView.findViewById(R.id.event_rating);
            mReadTextView = itemView.findViewById(R.id.read_text_view);
            mReadImageView = itemView.findViewById(R.id.read_image_view);
            mOptionsImageView = itemView.findViewById(R.id.options_image_view);
            mStorage = FirebaseStorage.getInstance().getReference(
                    Const.DB.DB_IMAGE + FirebaseAuth.getInstance().getUid() + Const.DB.DB_EVENT_IMAGES);
            itemView.setOnClickListener(this);
        }

        /**
         * Получаем пользователя.
         * Заполняем поля данными пользователя.
         */
        private void bind(Event event) {
            mEvent = event;
            mDateTextView.setText(DateFormat.format(Const.Date.DATE_TIME_FORMAT, mEvent.getDate()));
            mTitleTextView.setText(mEvent.getTitle());
            mDescriptionTextView.setText(getDescription(mEvent.getDescription()));
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
            mReadTextView.setText(R.string.read_more);
            mReadTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDescriptionTextView.setText(mEvent.getDescription());
                }
            });
            mOptionsImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getOptions();
                }
            });
            if (mEvent.getImage() != null) {
                Utils.ImageIn.showImageView(mStorage, mImageView, mEvent.getImage());
                mImageView.setPadding(0, 20, 0, 30);
            } else {
                mImageView.setImageDrawable(null);
                mImageView.setPadding(0, 0, 0, 5);
            }
        }

        private void getOptions() {
            FragmentManager manager = getFragmentManager();
            OptionsEventFragment dialog = OptionsEventFragment.newInstance(mEvent.getKey(), mEvent.getImage());
            dialog.setTargetFragment(EventListFragment.this, REQUEST_EVENT);
            dialog.show(Objects.requireNonNull(manager), DIALOG_OPTIONS);
        }

        /**
         * Передаем данные события через интент в EventPagerActivity для
         * дальнейшего отображения в EventFragment.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), EventPagerActivity.class);
            intent.putExtra(EXTRA_EVENT_KEY, mEvent.getKey());
            startActivity(intent);
        }
    }

    private void disableReadViews() {
        mReadTextView.setTextSize(0);
        mReadImageView.setImageDrawable(null);
    }

    /**
     * Заполняем список Events объектами с БД
     * по UID пользователя и дате.
     * Создаем экземпляр Адаптера и
     * устанавливаем его RecyclerView.setAdapter(Адаптер).
     */
    private void updateUI() {
        mEvents = new ArrayList<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Const.DB.DB_EVENTS);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mEvents.clear();
                String mUserUID = FirebaseAuth.getInstance().getUid();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    assert mUserUID != null;
                    if (mUserUID.equals(Objects.requireNonNull(
                            dataSnapshot.getValue(Event.class)).getUserUID())) {
                        if (DateFormat.format(DATE_DAY_MONTH, mDate)
                                .equals(DateFormat.format(DATE_DAY_MONTH,
                                        Objects.requireNonNull(dataSnapshot.getValue(Event.class)).getDate()))) {
                            Event event = dataSnapshot.getValue(Event.class);
                            mEvents.add(event);
                        }
                    }
                }
                removeClearListViews();
                mAdapter = new EventAdapter();
                mEventRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG_EVENT_LIST, "onCancelled: " + error.getMessage());
            }
        });
    }

    /**
     * Устанавливаем выбранную в календаре дату.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_DATE &&
                resultCode == Activity.RESULT_OK &&
                data != null) {
            mDate = (Date) data.getSerializableExtra(Const.Extra.EXTRA_EVENT_DATE);
            updateUI();
        }
    }

    private void removeClearListViews() {
        if (mEvents.isEmpty()) {
            mDateTextView.setText(DateFormat.format(DATE_FORMAT, mDate));
            mClearListImageView.setImageResource(R.drawable.clear_bg);
            mClearList.setText(R.string.list_clear);
            mEventsClear.setText(R.string.empty_events);
            mAddEventImageButton.setImageResource(R.drawable.ic_add_circle_blue);
            mAddEventImageButton.setClickable(true);
        } else {
            mDateTextView.setText(null);
            mClearListImageView.setImageDrawable(null);
            mClearList.setText(null);
            mEventsClear.setText(null);
            mAddEventImageButton.setImageDrawable(null);
            mAddEventImageButton.setClickable(false);
        }
    }

    /**
     * Запуск Activity для создание нового события.
     */
    private void newEvent() {
        Intent intent = new Intent(getActivity(), AddEventActivity.class);
        intent.putExtra(Const.Extra.EXTRA_EVENT_DATE, mDate);
        startActivity(intent);
    }

    private String getDescription(String description) {
        if (description.length() > 180) {
            return description.substring(0, 180) + "... ";
        } else {
            disableReadViews();
            return description;
        }
    }

    /**
     * Запуск диологового окна с календарем.
     */
    private void getDatePickerDialog() {
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialogDate = DatePickerFragment.newInstance(new Date());
        dialogDate.setTargetFragment(EventListFragment.this, REQUEST_DATE);
        dialogDate.show(Objects.requireNonNull(manager), DIALOG_DATE);
    }

    /**
     * Запуск профиля пользователя.
     */
    private void getProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Счётчик событий.
     * Отображается в ActionBar под названием Activity.
     */
    private void updateSubtitle() {
        String subtitle = getString(R.string.events) + " " + mEvents.size();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Const.Key.KEY_INDEX, mDate);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_list, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_event:
                newEvent();
                return true;
            case R.id.profile_user:
                getProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
