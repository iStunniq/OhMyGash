package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MyProfile extends AppCompatActivity {

    private TextView Title,Nametxt,Emailtxt,Passwordtxt,Confirmtxt,Veriftxt;
    private EditText Name,Email,Password,Confirm;
    private Button Save,Place,Menu,Verif;
    private FirebaseAuth FBAuth;
    private FirebaseUser FBUser;
    private DatabaseReference currentUser;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    private String OldName,OldEmail,OldPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Title = findViewById(R.id.MyProfileText);
        Nametxt = findViewById(R.id.ProfileNameText);
        Emailtxt = findViewById(R.id.ProfileEmailText);
        Passwordtxt = findViewById(R.id.ProfilePasswordText);
        Confirmtxt = findViewById(R.id.OldPasswordText);
        Name = findViewById(R.id.ProfileName);
        Email = findViewById(R.id.ProfileEmail);
        Password = findViewById(R.id.ProfilePassword);
        Confirm = findViewById(R.id.OldPassword);
        Save = findViewById(R.id.ProfileSave);
        Place = findViewById(R.id.GoToPlaceProfile);
        Menu = findViewById(R.id.ProfileMenu);
        Verif = findViewById(R.id.RequestVerification);
        Veriftxt = findViewById(R.id.VerificationStatus);

        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        currentUser = userRef.child(FBUser.getUid());

        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                OldName = user.child("name").getValue().toString();
                OldEmail = user.child("email").getValue().toString();
                OldPassword = user.child("pass").getValue().toString();
                Name.setText(OldName);
                Email.setText(OldEmail);
                String accType = user.child("accType").getValue().toString();
                if (!accType.matches("General") && !accType.matches("Admin")) {
                    Place.setVisibility(View.VISIBLE);
                    Place.setText("My " + accType);
                    Place.setOnClickListener(view -> {
                        Intent intent = new Intent(MyProfile.this, PlaceProfile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });

                    if (user.child("status").getValue() != null) {
                        String status = user.child("status").getValue().toString();
                        Verif.setVisibility(View.VISIBLE);
                        Veriftxt.setVisibility(View.VISIBLE);
                        Veriftxt.setText("Status: " +status);

                        Verif.setOnClickListener(view->{
                            currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.child("status").getValue().toString().matches("Unverified")){
                                        Veriftxt.setText("Status: Requested");
                                        user.child("status").getRef().setValue("Requested");
                                        Toast.makeText(MyProfile.this, "Validation Request Sent. Please wait for a response in your email", Toast.LENGTH_LONG).show();
                                    } else if(snapshot.child("status").getValue().toString().matches("Requested")){
                                        Toast.makeText(MyProfile.this, "Validation Request Already Sent. Please wait for a response in your email", Toast.LENGTH_LONG).show();
                                    } else if(snapshot.child("status").getValue().toString().matches("Verified")){
                                        Toast.makeText(MyProfile.this, "Account already verified. You may now use the inventory functions", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Save.setOnClickListener(view->{
                String NameText = Name.getText().toString();
                String PassText = Password.getText().toString();
                if (Name.getText() == null || NameText.matches("")) {
                    Toast.makeText(MyProfile.this, "Fields must not be blank", Toast.LENGTH_SHORT).show();
                } else if (PassText.length()>0 && PassText.length() < 6) {
                    Toast.makeText(MyProfile.this, "Password must be atleast 6 characters long", Toast.LENGTH_SHORT).show();
                }
                else if (!OldPassword.matches(Confirm.getText().toString())) {
                    Toast.makeText(MyProfile.this, "Wrong Confirmation Password", Toast.LENGTH_SHORT).show();
                } else {
                    if (!OldName.matches(NameText.trim())) {
                        currentUser.child("name").setValue(NameText);
                    }
                    if (!PassText.matches(OldPassword.trim()) && PassText.length()>0) {
                        currentUser.child("pass").setValue(PassText);
                        FBUser.reauthenticate(EmailAuthProvider.getCredential(OldEmail,OldPassword))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FBUser.updatePassword(PassText);
                                        }
                                    }
                                });
                    }
                    Toast.makeText(MyProfile.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(this.getIntent());
                }
        });

        Menu.setOnClickListener(view->{
            Intent intent = new Intent(MyProfile.this,Menu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);;
        });
    }
}