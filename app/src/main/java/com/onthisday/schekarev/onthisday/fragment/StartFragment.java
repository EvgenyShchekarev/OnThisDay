package com.onthisday.schekarev.onthisday.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onthisday.schekarev.onthisday.activity.MainActivity;
import com.onthisday.schekarev.onthisday.activity.R;
import com.onthisday.schekarev.onthisday.model.User;
import com.onthisday.schekarev.onthisday.utils.Const;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Date;
import java.util.Objects;

/**
 * Start приложения.
 * Фрагмент Регистрации и Авторизации.
 */
public class StartFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout mRoot;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        Button signInButton = view.findViewById(R.id.sign_in_button);
        Button registerButton = view.findViewById(R.id.register_button);
        mRoot = view.findViewById(R.id.root_element);

        signInButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(Const.DB.DB_USERS);

        // Автоматическая авторизация,
        // если пользователь был авторизован ранее.
        if (mAuth.getUid() != null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            if (getActivity() != null) getActivity().finish();
        }
        return view;
    }

    /**
     * Вызов методов Регистрации и Авторизации при нажатии на кнопки.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.register_button:
                registerUsers();
                break;
        }
    }

    /**
     * Авторизация в AlertDialog.
     */
    private void signIn() {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_sign_in, null);
        final MaterialEditText email = view.findViewById(R.id.email_field);
        final MaterialEditText password = view.findViewById(R.id.password_field);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.authorization);
        dialog.setMessage(R.string.login_and_password);
        dialog.setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(email.getText())) {
                            snackBarMessage(R.string.email);
                            return;
                        }
                        if (Objects.requireNonNull(password.getText()).length() < 4) {
                            snackBarMessage(R.string.password);
                            return;
                        }
                        mAuth.signInWithEmailAndPassword(
                                email.getText().toString().trim(),
                                password.getText().toString().trim())
                                .addOnCompleteListener(Objects.requireNonNull(getActivity()),
                                        new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                    getActivity().finish();
                                                } else {
                                                    snackBarMessage(R.string.activation_failed);
                                                }
                                            }
                                        });
                    }
                }).show();
    }

    /**
     * Регистрация в AlertDialog.
     */
    public void registerUsers() {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_registration, null);
        final MaterialEditText name = view.findViewById(R.id.name_field);
        final MaterialEditText email = view.findViewById(R.id.email_field);
        final MaterialEditText password = view.findViewById(R.id.password_field);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.registration);
        dialog.setMessage(R.string.date_for_register);
        dialog.setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(name.getText())) {
                            snackBarMessage(R.string.name);
                            return;
                        }
                        if (TextUtils.isEmpty(email.getText())) {
                            snackBarMessage(R.string.email);
                            return;
                        }
                        if (Objects.requireNonNull(password.getText()).length() < 5) {
                            snackBarMessage(R.string.password);
                            return;
                        }

                        // Создаем объект.
                        final User user = new User();
                        user.setName(name.getText().toString());
                        user.setEmail(email.getText().toString());
                        user.setPass(password.getText().toString());
                        user.setBirthDate(new Date());
                        user.setRegDate(new Date());

                        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPass())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance()
                                                .getCurrentUser())
                                                .getUid())
                                                .setValue(user);
                                        snackBarMessage(R.string.registration_successfully);
                                    }
                                });
                    }
                });
        dialog.show();
    }

    private void snackBarMessage(int message) {
        Snackbar.make(mRoot, message, Snackbar.LENGTH_SHORT).show();
    }
}
