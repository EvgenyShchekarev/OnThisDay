package com.onthisday.schekarev.onthisday.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.onthisday.schekarev.onthisday.activity.R;

import java.util.Objects;

/**
 * Фрагмент диалогового окна ProgressBar.
 */
public class LoadingFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_progressbar, null);

        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(view)
                .setCancelable(false)
                .create();
    }
}
