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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GeneralRegister extends AppCompatActivity {

    private EditText Name, Password, Email;
    private Button Return, Register;
    private FirebaseAuth FBAuth;


    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_register);

        FBAuth = FirebaseAuth.getInstance();

        Name = findViewById(R.id.generalRegisterName);
        Password = findViewById(R.id.generalRegisterPassword);
        Email = findViewById(R.id.generalRegisterEmail);
        Register = findViewById(R.id.RegisterNewGeneral);
        Return = findViewById(R.id.StationToRegister);


        Register.setOnClickListener(view -> {
            final String nameTxt = Name.getText().toString().trim();
            final String passwordTxt = Password.getText().toString().trim();
            final String emailTxt = Email.getText().toString().trim();

            if (nameTxt.isEmpty() || nameTxt == "" || passwordTxt.isEmpty() || passwordTxt == "" || emailTxt.isEmpty() || emailTxt == "") {
                Toast.makeText(GeneralRegister.this,"Please Fill in All Entries",Toast.LENGTH_LONG).show();
            }
            else if (passwordTxt.length() < 6 || passwordTxt.contains(" ")){
                Toast.makeText(GeneralRegister.this,"Password must be atleast 6 characters long and contains no spaces",Toast.LENGTH_LONG).show();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()){
                Toast.makeText(GeneralRegister.this, "Please Write a Valid Email", Toast.LENGTH_SHORT).show();
            }
            else {

                User user = new User(emailTxt,passwordTxt,nameTxt,"General");
                createAcc(user);

//                databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild(emailTxt)){
//                            Toast.makeText(GeneralRegister.this,"This Email is Already In Use",Toast.LENGTH_LONG).show();
//                        } else {
//                            createAcc(user);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//
//                });
//                Toast.makeText(GeneralRegister.this, "Feedback", Toast.LENGTH_SHORT).show();
            }

        });
        Return.setOnClickListener(view -> {
            Intent intent = new Intent(GeneralRegister.this,Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                            Toast.makeText(GeneralRegister.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser FBuser = FBAuth.getCurrentUser();
                            if (FBuser != null) {
                                databaseReference.child(FBuser.getUid()).setValue(user);
                            }
                            Intent intent = new Intent(GeneralRegister.this, Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(GeneralRegister.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
