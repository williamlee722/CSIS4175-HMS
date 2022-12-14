package com.example.howsMyStylist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.howsMyStylist.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "test";
    private EditText edit_username, edit_firstname, edit_lastname ,edit_email, edit_phone,
            edit_password, edit_confirm_password;
    private TextView edit_birthday;
    private Button btn_createAccount, btn_login;
    private CheckBox checkBoxAgree;
    private DatePickerDialog picker;
    // Progress bar
    private ProgressBar loadingPB;
    boolean isProgressVisible = false;

    private DatabaseReference mFirebaseDatabase; //we need a reference to make connection
    private FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // xml references
        edit_username = findViewById(R.id.edit_username);
        edit_firstname = findViewById(R.id.edit_firstname);
        edit_lastname = findViewById(R.id.edit_lastname);
        edit_email = findViewById(R.id.edit_email);
        edit_phone = findViewById(R.id.edit_phone);
        edit_password = findViewById(R.id.edit_password);
        edit_confirm_password = findViewById(R.id.edit_password_confirm);
        checkBoxAgree = findViewById(R.id.checkbox_agree);
        btn_login = findViewById(R.id.btn_registerLogin);
        btn_createAccount = findViewById(R.id.btn_createAccount);
        // In case need to select dob in this page ( default is today's date)
        edit_birthday = findViewById(R.id.edit_reg_birthday);
        //set up DatePicker on EditText
        edit_birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            //Date Picker Dialog
            picker = new DatePickerDialog(RegisterActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    edit_birthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                }
            }, year, month, day);
            picker.show();
        });

        // TODO: Back to login
        btn_login.setOnClickListener(v -> {
//            Toast.makeText(RegisterActivity.this, "Join us today", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
        // TODO: Create progress bar
        loadingPB = findViewById(R.id.progress_circular_loading);

        // TODO: Create Account
        btn_createAccount.setOnClickListener(v -> {
            // Obtain the entered data
            String username = edit_username.getText().toString();
            String email = edit_email.getText().toString();
            String phone = edit_phone.getText().toString();
            String pwd = edit_password.getText().toString();
            String confirm_pwd = edit_confirm_password.getText().toString();
            String birthday = edit_birthday.getText().toString();
            String firstname = edit_firstname.getText().toString();
            String lastname = edit_lastname.getText().toString();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your username.", Toast.LENGTH_SHORT).show();
                edit_username.setError("Username is required.");
                edit_username.requestFocus();
            } else if (TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your email.", Toast.LENGTH_SHORT).show();
                edit_email.setError("Email is required.");
                edit_email.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your email.", Toast.LENGTH_SHORT).show();
                edit_email.setError("Valid email is required.");
                edit_email.requestFocus();
            } else if (TextUtils.isEmpty(phone)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                edit_phone.setError("Phone number is required.");
                edit_phone.requestFocus();
            } else if (phone.length() != 10) {
                Toast.makeText(RegisterActivity.this,
                        "Please re-enter enter your phone number.", Toast.LENGTH_SHORT).show();
                edit_phone.setError("Phone number should be 10 digits.");
                edit_phone.requestFocus();
            } else if (TextUtils.isEmpty(pwd)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your password.", Toast.LENGTH_SHORT).show();
                edit_password.setError("Password is required.");
                edit_password.requestFocus();
            } else if (pwd.length() < 6) {
                Toast.makeText(RegisterActivity.this,
                        "Password should be at least 6 digits.", Toast.LENGTH_SHORT).show();
                edit_password.setError("Password too weak.");
                edit_password.requestFocus();
            } else if (TextUtils.isEmpty(confirm_pwd)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter your password again.", Toast.LENGTH_SHORT).show();
                edit_confirm_password.setError("Password confirmation is required.");
                edit_confirm_password.requestFocus();
            } else if (!pwd.equals(confirm_pwd)) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter same password.", Toast.LENGTH_SHORT).show();
                edit_confirm_password.setError("Password confirmation is required.");
                edit_confirm_password.requestFocus();
                // Clean the entered passwords
                edit_password.clearComposingText();
                edit_confirm_password.clearComposingText();
            } else if (!checkBoxAgree.isChecked()) {
                Toast.makeText(RegisterActivity.this,
                        "Please confirm term and policy.", Toast.LENGTH_SHORT).show();

            } else {
                registerUser(username, email, phone, pwd, birthday, firstname, lastname);

                if (isProgressVisible){
                    loadingPB.setVisibility(View.GONE);
                    isProgressVisible = false;
                } else {
                    isProgressVisible = true;
                    loadingPB.setVisibility(View.VISIBLE);
                }
            }
        });



    }

    private void registerUser(String username, String email, String phone, String pwd, String birthday, String firstname, String lastname) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // create user profile
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Update display name of user
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    // Enter user data into the firebase realtime db
                    User newUser = new User(username, email, phone, pwd, birthday, firstname, lastname);

                    //Extracting user reference for registered users
                    mFirebaseInstance = FirebaseDatabase.getInstance();
                    mFirebaseDatabase = mFirebaseInstance.getReference("User");
                    mFirebaseDatabase.child(firebaseUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                // Send Verification Email
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                // Open User Profile after successful registration?? UserProfileActivity
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                // Prevent user back to Register activity
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "User registered failed! Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        edit_password.setError("Your password is too weak! Kindly use a mix use of alphabets, numbers and special characters.");
                        edit_password.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        edit_email.setError("Your email is invalid or already in use!");
                        edit_email.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        edit_email.setError("User is already registered with this email, please use another email!");
                        edit_email.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}