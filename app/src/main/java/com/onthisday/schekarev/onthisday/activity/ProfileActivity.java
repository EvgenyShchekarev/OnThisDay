package com.onthisday.schekarev.onthisday.activity;

import androidx.fragment.app.Fragment;

import com.onthisday.schekarev.onthisday.fragment.ProfileFragment;
import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;

public class ProfileActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ProfileFragment();
    }
}
