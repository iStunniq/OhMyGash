package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class StationRegister extends AppCompatActivity {

    private EditText Name, Password, Email, StationName, StationAddress;
    private Button Return, Register;
    private Spinner Brand;

    private FirebaseAuth FBAuth;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_register);

        FBAuth = FirebaseAuth.getInstance();

        Name = findViewById(R.id.stationRegisterName);
        Password = findViewById(R.id.stationRegisterPassword);
        Email = findViewById(R.id.stationRegisterEmail);
        StationName = findViewById(R.id.stationRegisterStationName);
        StationAddress = findViewById(R.id.stationRegisterStationAddress);
        Register = findViewById(R.id.RegisterNewStation);
        Return = findViewById(R.id.StationToRegister);
        Brand = findViewById(R.id.BrandForRegister);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.station_array, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        Brand.setAdapter(adapter);

        Return.setOnClickListener(view -> {
            Intent intent = new Intent(StationRegister.this,Register.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        Register.setOnClickListener(view -> {
            Toast.makeText(StationRegister.this, "Checking Credentials", Toast.LENGTH_SHORT).show();
            final String nameTxt = Name.getText().toString().trim();
            final String passwordTxt = Password.getText().toString().trim();
            final String emailTxt = Email.getText().toString().trim();
            final String stNameTxt = StationName.getText().toString().trim();
            final String stAddTxt = StationAddress.getText().toString().trim();

            if (nameTxt.isEmpty() || nameTxt == "" || passwordTxt.isEmpty() || passwordTxt == "" || emailTxt.isEmpty() || emailTxt == "" || stNameTxt.isEmpty() || stNameTxt == "" || stAddTxt.isEmpty() || stAddTxt == "" || Brand.getSelectedItem().toString().matches("None")) {
                Toast.makeText(StationRegister.this,"Please Fill in All Entries",Toast.LENGTH_LONG).show();
            }
            else if (passwordTxt.length() < 6 || passwordTxt.contains(" ")){
                Toast.makeText(StationRegister.this,"Password must be atleast 6 characters long and contains no spaces",Toast.LENGTH_LONG).show();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()){
                Toast.makeText(StationRegister.this, "Please Write a Valid Email", Toast.LENGTH_SHORT).show();
            }
            else {

                User user = new User(emailTxt,passwordTxt,nameTxt,stNameTxt,stAddTxt, Brand.getSelectedItem().toString(),"Unverified","Station");
                createAcc(user);

            }

        });
        Return.setOnClickListener(view -> {
            Intent intent = new Intent(StationRegister.this,Register.class);
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
                            Toast.makeText(StationRegister.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser FBuser = FBAuth.getCurrentUser();
                            if (FBuser != null) {
                                databaseReference.child(FBuser.getUid()).setValue(user);
                            }
                            Intent intent = new Intent(StationRegister.this, Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(StationRegister.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}