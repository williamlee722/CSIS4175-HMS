package com.example.howsMyStylist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.howsMyStylist.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private EditText edit_email, edit_password;
    private Button btn_login, btn_register, btn_forgotPassword;
    private FirebaseAuth auth;
    private final String TAG = "LoginActivity";

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edit_email = findViewById(R.id.edit_loginEmail);
        edit_password = findViewById(R.id.edit_loginPassword);
        btn_login = findViewById(R.id.btn_login);

        // Register page
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Register page clicked", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

        });

        // Reset password
        btn_forgotPassword = findViewById(R.id.btn_forgotPassword);
        btn_forgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "You can reset your password now!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Auth
        auth = FirebaseAuth.getInstance();

        btn_login.setOnClickListener(v -> {
            String text_loginEmail = edit_email.getText().toString();
            String text_loginPassword = edit_password.getText().toString();
            if (TextUtils.isEmpty(text_loginEmail)){
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                edit_email.setError("Email is required.");
                edit_email.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(text_loginEmail).matches()) {
                Toast.makeText(LoginActivity.this,
                        "Please enter your email.", Toast.LENGTH_SHORT).show();
                edit_email.setError("Valid email is required.");
                edit_email.requestFocus();
            } else if (TextUtils.isEmpty(text_loginPassword)) {
                Toast.makeText(LoginActivity.this,
                        "Please enter your password.", Toast.LENGTH_SHORT).show();
                edit_password.setError("Password is required.");
                edit_password.requestFocus();
            } else {
                loginUser(text_loginEmail,text_loginPassword);
            }
        });

//        // init firebase
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference table_user = database.getReference("User");
//
//        btn_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                table_user.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        //check user validation
//                        if(snapshot.child(edit_email.getText().toString()).exists()){
//                            //get user information
//                            User user = snapshot.child(edit_email.getText().toString()).getValue(User.class);
//                            if(user.getPassword().equals(edit_password.getText().toString())){
//                                Toast.makeText(LoginActivity.this, "Username or Password success", Toast.LENGTH_LONG).show();
//                            }else{
//                                Toast.makeText(LoginActivity.this, "Username or Password failed", Toast.LENGTH_LONG).show();
//                            }
//                        }else{
//                            Toast.makeText(LoginActivity.this, "Username or Password failed", Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//        });
    }

    private void loginUser(String loginEmail, String loginPassword) {
        auth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Get instance of current user
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    // Check if the user is verified before user can access their profile
                    if (firebaseUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                        // Open Home page
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        // Prevent user back to Register activity
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        firebaseUser.sendEmailVerification();
                        auth.signOut();
                        showAlertDialog();
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        edit_email.setError("User is not existed. Please re-enter the email or register.");
                        edit_email.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        edit_password.setError("Invalid password. Please check and re-enter.");
                        edit_password.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void showAlertDialog() {
        //Set up alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("You account is not verified!");
        builder.setMessage("Please verify your email now. You cannot login without email verification.");
        //Open email app if "continue" clicked
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //open in a new window
                startActivity(intent);
            }
        });
        //Create AlertDialog
        AlertDialog alertDialog = builder.create();
        //Show AlertDialog
        alertDialog.show();
    }

    //check if user is logg in now. -> take to ???Activity
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this, "Already logged in!", Toast.LENGTH_SHORT).show();
            //start Home activity
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "You can log in now!", Toast.LENGTH_SHORT).show();
        }

    }

}