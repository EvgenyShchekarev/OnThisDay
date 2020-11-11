package com.onthisday.schekarev.onthisday.activity;

import androidx.fragment.app.Fragment;

import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;
import com.onthisday.schekarev.onthisday.fragment.StartFragment;

public class StartActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new StartFragment();
    }
}