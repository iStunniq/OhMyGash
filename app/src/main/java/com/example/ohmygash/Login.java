package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText Email, Password;
    private Button LoginButton, RegisterButton;
    private FirebaseAuth FBAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FBAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FBAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(Login.this,Map.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        Email = findViewById(R.id.EmailBox);
        Password = findViewById(R.id.PasswordBox);
        LoginButton = findViewById(R.id.LoginButton);
        RegisterButton = findViewById(R.id.RegisterButton);
        LoginButton.setOnClickListener(view -> {
            final String emailTxt = Email.getText().toString().trim();
            final String passwordTxt = Password.getText().toString().trim();

            if (emailTxt.isEmpty()){
                Toast.makeText(Login.this, "Please Fill Email", Toast.LENGTH_LONG).show();
            }   else    {
                FBAuth.signInWithEmailAndPassword(emailTxt,passwordTxt).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this,Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(Login.this, "Login Failed: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//                dbref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild(emailTxt)) {
//
//                            String getpass = snapshot.child(emailTxt).child("pass").getValue(String.class);
//
//                            if (getpass.equals(passwordTxt)){
//                                Toast.makeText(Login.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(Login.this, "Password is Incorrect", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(Login.this, "This Username Does Not Exist", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }

        });

        RegisterButton.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this,Register.class);
            startActivity(intent);
        });



    }
}