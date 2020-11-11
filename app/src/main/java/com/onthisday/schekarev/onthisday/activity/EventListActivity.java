package com.onthisday.schekarev.onthisday.activity;

import androidx.fragment.app.Fragment;

import com.onthisday.schekarev.onthisday.fragment.EventListFragment;
import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;

/**
 * Хост для EventListFragment.
 */
public class EventListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new EventListFragment();
    }
}