package com.onthisday.schekarev.onthisday.activity;

import androidx.fragment.app.Fragment;
import com.onthisday.schekarev.onthisday.fragment.EventFragment;
import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;

/**
 * Хост для EventDetailPagerFragment и DatePickerFragment.
 */
public class EventPagerActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new EventFragment();
    }
}
