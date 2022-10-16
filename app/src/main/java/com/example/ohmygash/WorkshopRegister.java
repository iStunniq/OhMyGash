package com.example.ohmygash;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class WorkshopRegister extends AppCompatActivity {

    private EditText Name, Password, Email, WorkName, WorkAddress;
    private Button Return, Register;

    private FirebaseAuth FBAuth;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_register);

        FBAuth = FirebaseAuth.getInstance();

        Name = findViewById(R.id.workshopRegisterName);
        Password = findViewById(R.id.workshopRegisterPassword);
        Email = findViewById(R.id.workshopRegisterEmail);
        WorkName = findViewById(R.id.workshopRegisterWorkshopName);
        WorkAddress = findViewById(R.id.workshopRegisterWorkshopAddress);
        Register = findViewById(R.id.RegisterNewWorkshop);
        Return = findViewById(R.id.WorkshopToRegister);

        Return.setOnClickListener(view -> {
            Intent intent = new Intent(WorkshopRegister.this, Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        Register.setOnClickListener(view -> {
            final String nameTxt = Name.getText().toString().trim();
            final String passwordTxt = Password.getText().toString().trim();
            final String emailTxt = Email.getText().toString().trim();
            final String workNameTxt = WorkName.getText().toString().trim();
            final String workAddTxt = WorkAddress.getText().toString().trim();

            if (nameTxt.isEmpty() || nameTxt == "" || passwordTxt.isEmpty() || passwordTxt == "" || emailTxt.isEmpty() || emailTxt == "" || workNameTxt.isEmpty() || workNameTxt == "" || workAddTxt.isEmpty() || workAddTxt == "") {
                Toast.makeText(WorkshopRegister.this, "Please Fill in All Entries", Toast.LENGTH_LONG).show();
            } else if (passwordTxt.length() < 6 || passwordTxt.contains(" ")) {
                Toast.makeText(WorkshopRegister.this, "Password must be atleast 6 characters long and contains no spaces", Toast.LENGTH_LONG).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                Toast.makeText(WorkshopRegister.this, "Please Write a Valid Email", Toast.LENGTH_SHORT).show();
            } else {

                User user = new User(emailTxt, passwordTxt, nameTxt, workNameTxt, workAddTxt, "Workshop");

                createAcc(user);
//                Toast.makeText(WorkshopRegister.this, "Feedback", Toast.LENGTH_SHORT).show();
            }

        });
        Return.setOnClickListener(view -> {
            Intent intent = new Intent(WorkshopRegister.this, Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

    }

    public void createAcc(User user) {
        FBAuth = FirebaseAuth.getInstance();
        FBAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPass())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(WorkshopRegister.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser FBuser = FBAuth.getCurrentUser();
                            if (FBuser != null) {
                                databaseReference.child(FBuser.getUid()).setValue(user);
                            }
                            Intent intent = new Intent(WorkshopRegister.this, Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(WorkshopRegister.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}