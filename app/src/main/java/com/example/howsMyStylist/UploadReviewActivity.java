package com.example.howsMyStylist;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.example.howsMyStylist.Adapter.ImagesAdapter;
import com.example.howsMyStylist.Model.Review;
import com.example.howsMyStylist.Model.Salon;
import com.example.howsMyStylist.Model.Stylist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UploadReviewActivity extends AppCompatActivity {

    private CurrencyEditText currencyEditText;
    private EditText edit_stylistName, edit_salonName, edit_serviceName,
                        edit_serviceDate, edit_review;
    private RatingBar ratingBar;
    private ViewPager viewPager;
    private final int REQUEST_PERMISSION_CODE = 35;
    private Button btn_addPhoto, btn_submit;

    private DatePickerDialog picker;
    private String date;

    private FirebaseAuth auth;
    private StorageReference reviewReference;
    private DatabaseReference firebaseDatabase;
    private FirebaseDatabase firebaseInstance;
    private FirebaseUser firebaseUser;
    private ActivityResultLauncher<String> imgFilePicker;
    private List<Uri> uriUploadImgs;
    private int uploadImgCount = 0;
    private byte[] imageBytes;

    private String reviewId;
    private String _PHOTO;
    private List<String> photosList;
    private int reviewWithStylistCount;
    private int reviewWithSalonCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_review);

        //onEditListener and PopupWindow
        edit_stylistName = findViewById(R.id.input_stylistName);
        edit_salonName = findViewById(R.id.input_salonName);
        edit_stylistName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChooseStylistActivity.class);
                startActivity(intent);
            }
        });

        edit_serviceName = findViewById(R.id.input_serviceName);
        edit_review = findViewById(R.id.input_review);
        ratingBar = findViewById(R.id.ratingBar);

        //getString from popup activity
        Intent intent = getIntent();
        edit_stylistName.setText(intent.getStringExtra("stylistName"));
        edit_salonName.setText(intent.getStringExtra("salonName"));

        //price formatter
        currencyEditText = findViewById(R.id.input_price);

        //Date picker
        edit_serviceDate = findViewById(R.id.input_serviceDate);
        edit_serviceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extracting to dd,mm,yyyy by /
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(UploadReviewActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        edit_serviceDate.setText(date);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        //add images
        viewPager = findViewById(R.id.viewPager);
        btn_addPhoto = findViewById(R.id.btnAddImages);
        uriUploadImgs = new ArrayList<>();

        btn_addPhoto.setOnClickListener(v -> {
            checkUserPermission();
            if (uriUploadImgs == null) {
                btn_addPhoto.setText("Add Images");
                viewPager.setVisibility(View.GONE);
            } else {
                btn_addPhoto.setText("Choose others");
                viewPager.setVisibility(View.VISIBLE);
            }
        });

        imgFilePicker = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), new ActivityResultCallback<List<Uri>>() {
            @Override
            public void onActivityResult(List<Uri> result) {
                uriUploadImgs = result;
                setAdapter();
            }
        });

        //submit
        btn_submit = findViewById(R.id.btnSubmitReview);
        btn_submit.setOnClickListener(v -> {
            String stylistName = edit_stylistName.getText().toString();
            String salonName = edit_salonName.getText().toString();
            String serviceName = edit_serviceName.getText().toString();
            Double price = currencyEditText.getNumericValue();
            //date: we have already assigned when picking the date from the calendar
            String review = edit_review.getText().toString();
            double rating =  ratingBar.getRating();

            //check all required info
            if (TextUtils.isEmpty(stylistName)) {
                Toast.makeText(UploadReviewActivity.this,
                        "Please enter your stylist.", Toast.LENGTH_SHORT).show();
                edit_stylistName.setError("Stylist's name is required.");
                edit_stylistName.requestFocus();
            } else if (TextUtils.isEmpty(serviceName)) {
                Toast.makeText(UploadReviewActivity.this,
                        "Please enter the service name.", Toast.LENGTH_SHORT).show();
                edit_serviceName.setError("Service is required.");
                edit_serviceName.requestFocus();
            } else if (TextUtils.isEmpty(date)) {
                Toast.makeText(UploadReviewActivity.this,
                        "Please select the date.", Toast.LENGTH_SHORT).show();
                edit_serviceName.setError("Date is required.");
                edit_serviceName.requestFocus();
            } else if (TextUtils.isEmpty(price.toString())) {
                Toast.makeText(UploadReviewActivity.this,
                        "Please enter the price.", Toast.LENGTH_SHORT).show();
                currencyEditText.setError("Price is required.");
                currencyEditText.requestFocus();
            } else if (rating == 0) {
                Toast.makeText(UploadReviewActivity.this,
                        "Please rate your stylist.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UploadReviewActivity.this,
                        "Request send.", Toast.LENGTH_SHORT).show();
                firebaseInstance = FirebaseDatabase.getInstance();
                firebaseDatabase = firebaseInstance.getReference("Review");  //Review -> reviewId -> newReview
                reviewId = firebaseDatabase.push().getKey();
                uploadReview(stylistName, salonName, serviceName, price, date,
                                review, rating, uriUploadImgs);;
            }
        });

        // Initialize navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        FloatingActionButton fab = findViewById(R.id.fab);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        startActivity(new Intent(UploadReviewActivity.this, HomePageActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(UploadReviewActivity.this, UploadUserProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void checkUserPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        } else {
            pickImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImages();
            } else {
                Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImages() {
        imgFilePicker.launch("image/*");
        if (uriUploadImgs != null) {
            uriUploadImgs.clear();
        }
    }

    private void setAdapter() {
        ImagesAdapter imagesAdapter = new ImagesAdapter(this, uriUploadImgs);
        viewPager.setAdapter(imagesAdapter);
    }

    private void compressImages(List<Uri> uriUploadImgs) {
        photosList = new ArrayList<>();
        for (int i = 0; i < uriUploadImgs.size(); i++) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriUploadImgs.get(i));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                imageBytes = stream.toByteArray();
                _PHOTO = reviewId + "_" + i + ".jpg";
                photosList.add(_PHOTO);
                uploadImagesToFireStorage(imageBytes, uriUploadImgs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadReview(String stylistName, String salonName, String serviceName, double price, String date,
                              String review, double rating, List<Uri> uriUploadImgs) {

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        String pic = String.valueOf(firebaseUser.getPhotoUrl());

        Review newReview = new Review(stylistName, serviceName, price, date, rating, firebaseUser.getUid(), firebaseUser.getDisplayName(), pic);

        if (salonName != null) {
            newReview.setSalonName(salonName);
        }
        if (review != null) {
            newReview.setReview(review);
        }

        firebaseDatabase.child(reviewId).setValue(newReview).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //uploadImg to be children of review
                    if (uriUploadImgs != null) {
                        compressImages(uriUploadImgs); //compress and load to FireStorage
                        int i = 0;
                        for (String photo: photosList) {
                            firebaseDatabase.child(reviewId).child("images").child(String.valueOf(i)).setValue(photo);
                            i++;
                        }
                    }
                    // update User's reviewList
                    firebaseDatabase = firebaseInstance.getReference("User");
                    firebaseDatabase.child(firebaseUser.getUid()).child("reviewIdList").child(reviewId).setValue(stylistName+ ", " + salonName);

                    // update Stylist's avgRating
                    DatabaseReference reviewReference = FirebaseDatabase.getInstance().getReference("Review");
                    DatabaseReference stylistReference = FirebaseDatabase.getInstance().getReference("Stylist");
                    DatabaseReference salonReference = FirebaseDatabase.getInstance().getReference("Salon");
                    // 1. Count the num of stylist name in all reviews
                    reviewReference
                        .orderByChild("stylistName").equalTo(stylistName).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                reviewWithStylistCount = (int) snapshot.getChildrenCount();
                                //debug
                                System.out.println("reviewStylistCount: " + reviewWithStylistCount);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    // 2. use the name in the review to find the same name in stylist db and update avgRating
                    stylistReference
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getChildren().forEach(
                                    s -> {
                                        Stylist stylist = s.getValue(Stylist.class);
                                        String sName = stylist.getfName() + " " + stylist.getlName();
                                        if (stylistName.equals(sName)) {
                                            // debug
                                            System.out.println("Stylist: " + sName);
                                            System.out.println("rating set to: " + (stylist.getAvgRating() * (reviewWithStylistCount - 1) + rating) / (reviewWithStylistCount));
                                            stylist.setAvgRating( (stylist.getAvgRating() * (reviewWithStylistCount - 1) + rating) / (reviewWithStylistCount) );
                                            stylistReference.child(s.getKey()).setValue(stylist);
                                            System.out.println("rating: " + stylist.getAvgRating());
                                            Toast.makeText(UploadReviewActivity.this, "Stylist's rating updated", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                            );
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                    // update Salon's rating
                    // 1. Count the num of salon name in all reviews
                    reviewReference
                            .orderByChild("salonName").equalTo(salonName).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            reviewWithSalonCount = (int) snapshot.getChildrenCount();
                            //debug
                            System.out.println("reviewSalonCount: " + reviewWithSalonCount);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    // 2. use the name in the review to find the same name in stylist db and update avgRating
                    salonReference
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getChildren().forEach(
                                    s -> {
                                        Salon salon = s.getValue(Salon.class);
                                        String sName = salon.getSalonName();
                                        if (salonName.equals(sName)) {
                                            System.out.println("Salon: " + sName);
                                            System.out.println("rating set to: " + (salon.getAvgRating() * (reviewWithSalonCount - 1) + rating) / (reviewWithSalonCount));
                                            salon.setAvgRating( (salon.getAvgRating() * (reviewWithSalonCount - 1) + rating) / reviewWithSalonCount);
                                            salonReference.child(s.getKey()).setValue(salon);
                                            System.out.println("rating: " + salon.getAvgRating());
                                            Toast.makeText(UploadReviewActivity.this, "Salon's rating updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                    Toast.makeText(UploadReviewActivity.this, "Review posted successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UploadReviewActivity.this, HomePageActivity.class);
                    // Prevent user back
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UploadReviewActivity.this, "Review posted failed! Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImagesToFireStorage(byte[] imageBytes, List<Uri> uriUploadImgs) {
        reviewReference = FirebaseStorage.getInstance().getReference().child("ReviewPhotos");
        StorageReference imgReference = reviewReference.child(_PHOTO);

        imgReference.putBytes(imageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadImgCount++;
                if(uploadImgCount == uriUploadImgs.size()) {
                    Log.d("upload", "uploaded done");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadReviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

}