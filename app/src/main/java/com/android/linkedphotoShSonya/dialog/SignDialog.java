package com.android.linkedphotoShSonya.dialog;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.android.linkedphotoShSonya.R;
import com.android.linkedphotoShSonya.accounthelper.AccountHelper;
import com.android.linkedphotoShSonya.act.MainAppClass;
import com.android.linkedphotoShSonya.databinding.SignAppLayoutBinding;

import com.android.linkedphotoShSonya.db.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SignDialog {
    private FirebaseAuth auth;
    private Activity activity;
    private AccountHelper accountHelper;
    private SignAppLayoutBinding binding;//делаем снова bindin что б разгррузить код
    private AlertDialog dialog;
    private ArrayList<User> userArrayList;
    private MainAppClass mainAppClass;


    public SignDialog(FirebaseAuth auth, Activity activity, AccountHelper accountHelper) {
        this.auth = auth;
        this.activity = activity;
        this.accountHelper = accountHelper;
    }
    public void showSignDialog(int title, int buttonTitle, int index) {
        AlertDialog.Builder dialogBulder = new AlertDialog.Builder(activity);
        binding = SignAppLayoutBinding.inflate(activity.getLayoutInflater());
        dialogBulder.setView(binding.getRoot());
        binding.tvAlerTitle.setText(title);
    showForgetButton(index);
       binding.buttonSignUp.setText(buttonTitle);
        binding.buttonSignUp.setOnClickListener(onClickSignWithEmail(index));
        binding.bSignGoogle.setOnClickListener(onClickSignWithGoogle());
        binding.bForgetPassword.setOnClickListener(onClickForgetButton());
    dialog =dialogBulder.create();
        if(dialog.getWindow()!=null){
                dialog.getWindow().
    setBackgroundDrawableResource(android.R.color.transparent);}

        dialog.show();
}
    private void showForgetButton(int index) {
        if (index == 0) {
            binding.bForgetPassword.setVisibility(View.GONE);
            binding.imageId.setVisibility(View.VISIBLE);
            binding.edName.setVisibility(View.VISIBLE);
        } else {
            binding.bForgetPassword.setVisibility(View.VISIBLE);
            binding.imageId.setVisibility(View.GONE);
            binding.edName.setVisibility(View.GONE);

        }
    }
    private View.OnClickListener onClickSignWithEmail(final int index){
        return new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (index == 0) {
                                        Log.d("MyLog","For Index "+ index);
                                        accountHelper.signUp(binding.edEmail.getText().toString(),binding.edPassword.getText().toString(),binding.edName.getText().toString());
                                    } else {
                                        accountHelper.SignIn(binding.edEmail.getText().toString(), binding.edPassword.getText().toString());
                                    }
                                }
                            }
                        });

                    }
                }
                dialog.dismiss();
            }
        };
    }
    private View.OnClickListener onClickSignWithGoogle(){
        return new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    accountHelper.SignInGoogle(AccountHelper.GOOGLE_SIGN_IN_CODE);
                                }
                            }
                        });
                    }
                }
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener onClickForgetButton(){
        return new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
                if (binding.edPassword.isShown()) {
                    binding.edPassword.setVisibility(View.GONE);
                    binding.buttonSignUp.setVisibility(View.GONE);
                    binding.bSignGoogle.setVisibility(View.GONE);
                    binding.tvAlerTitle.setText(R.string.forget_password);
                    binding.bForgetPassword.setText(R.string.send_recet_password);
                    binding.tvMessage.setVisibility(View.VISIBLE);
                    binding.tvMessage.setText(R.string.forget_password_message);
                    // dialog.dismiss();
                } else {
                    if (!binding.edEmail.getText().toString().equals("")) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(binding.edEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(activity, R.string.email_is_send, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(activity, "Mistake", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(activity, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

}
