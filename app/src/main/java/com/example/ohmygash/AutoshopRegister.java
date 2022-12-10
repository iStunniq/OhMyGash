package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AutoshopRegister extends AppCompatActivity {

    private EditText Name, Password, Email, AutoName, AutoAddress;
    private Button Return, Register;

    private FirebaseAuth FBAuth;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoshop_register);

        FBAuth = FirebaseAuth.getInstance();

        Name = findViewById(R.id.autoshopRegisterName);
        Password = findViewById(R.id.autoshopRegisterPassword);
        Email = findViewById(R.id.autoshopRegisterEmail);
        AutoName = findViewById(R.id.autoshopRegisterAutoshopName);
        AutoAddress = findViewById(R.id.autoshopRegisterAutoshopAddress);
        Register = findViewById(R.id.RegisterNewAutoshop);
        Return = findViewById(R.id.AutoshopToRegister);

        Return.setOnClickListener(view -> {
            Intent intent = new Intent(AutoshopRegister.this, Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        Register.setOnClickListener(view -> {
            final String nameTxt = Name.getText().toString().trim();
            final String passwordTxt = Password.getText().toString().trim();
            final String emailTxt = Email.getText().toString().trim();
            final String workNameTxt = AutoName.getText().toString().trim();
            final String workAddTxt = AutoAddress.getText().toString().trim();

            if (nameTxt.isEmpty() || nameTxt == "" || passwordTxt.isEmpty() || passwordTxt == "" || emailTxt.isEmpty() || emailTxt == "" || workNameTxt.isEmpty() || workNameTxt == "" || workAddTxt.isEmpty() || workAddTxt == "") {
                Toast.makeText(AutoshopRegister.this, "Please Fill in All Entries", Toast.LENGTH_LONG).show();
            } else if (passwordTxt.length() < 6 || passwordTxt.contains(" ")) {
                Toast.makeText(AutoshopRegister.this, "Password must be atleast 6 characters long and contains no spaces", Toast.LENGTH_LONG).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                Toast.makeText(AutoshopRegister.this, "Please Write a Valid Email", Toast.LENGTH_SHORT).show();
            } else {

                User user = new User(emailTxt, passwordTxt, nameTxt, workNameTxt, workAddTxt, "Others", "Unverified", "Autoshop");

                createAcc(user);
//                Toast.makeText(AutoshopRegister.this, "Feedback", Toast.LENGTH_SHORT).show();
            }

        });
        Return.setOnClickListener(view -> {
            Intent intent = new Intent(AutoshopRegister.this, Register.class);
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
                            Toast.makeText(AutoshopRegister.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser FBuser = FBAuth.getCurrentUser();
                            if (FBuser != null) {
                                databaseReference.child(FBuser.getUid()).setValue(user);
                            }
                            Intent intent = new Intent(AutoshopRegister.this, Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AutoshopRegister.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}