package com.onthisday.schekarev.onthisday.activity;

import androidx.fragment.app.Fragment;

import com.onthisday.schekarev.onthisday.fragment.AddEventFragment;
import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;

/**
 * Хост для AddEventFragment.
 */
public class AddEventActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AddEventFragment();
    }
}
