package com.onthisday.schekarev.onthisday.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.onthisday.schekarev.onthisday.fragment.MainFragment;
import com.onthisday.schekarev.onthisday.fragment.SingleFragmentActivity;

/**
 * Хост для PinCodFragment.
 */

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем логотипа в ActionBar.
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setIcon(getDrawable(R.drawable.logo));*/
    }

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
