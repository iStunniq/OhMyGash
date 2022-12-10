package com.example.ohmygash;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PlaceProfile extends AppCompatActivity {

    private Button Upload, Back, Save;
    private TextView Title, Nametxt, Addresstxt, Confirmtxt, Brandtxt;
    private EditText Name, Address, Confirm;
    private ImageView Photo;
    private Spinner Brand;

    private ActivityResultLauncher<String> GetPhoto;
    private Uri imageUri,oldImageUri;
    private StorageReference photoRef = FirebaseStorage.getInstance().getReference("Photos");
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    private FirebaseAuth FBAuth;
    private FirebaseUser FBUser;
    private DatabaseReference currentUser;
    private StorageTask mUploadTask;

    private String OldName,OldAddress,OldPassword,OldBrand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_profile);

        Upload = findViewById(R.id.PlaceProfileUpload);
        Back = findViewById(R.id.PlaceProfileMenu);
        Save = findViewById(R.id.PlaceProfileSave);
        Title = findViewById(R.id.PlaceProfileText);
        Nametxt = findViewById(R.id.PlaceProfileNameText);
        Addresstxt = findViewById(R.id.PlaceProfileAddressText);
        Confirmtxt = findViewById(R.id.PlaceOldPasswordText);
        Brandtxt = findViewById(R.id.placeBrand);
        Name = findViewById(R.id.ProfilePlaceName);
        Address = findViewById(R.id.ProfilePlaceAddress);
        Confirm = findViewById(R.id.PlaceOldPassword);
        Photo = findViewById(R.id.PlaceProfilePhoto);
        Brand = findViewById(R.id.BrandForProfile);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.station_array, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        Brand.setAdapter(adapter);

        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        currentUser = userRef.child(FBUser.getUid());


        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                String accType = user.child("accType").getValue().toString();
                Title.setText("My "+ accType);
                Nametxt.setText(accType + " Name");
                Addresstxt.setText(accType + " Address");
                if (accType.matches("Autoshop")) {
                    Brand.setVisibility(View.GONE);
                    Brandtxt.setVisibility(View.GONE);
                }
                OldName = user.child("placeName").getValue().toString();
                OldAddress = user.child("placeAdd").getValue().toString();
                OldPassword = user.child("pass").getValue().toString();
                OldBrand = user.child("brand").getValue().toString();
                if (user.child("placePhoto").getValue() != null){
                    imageUri = Uri.parse(user.child("placePhoto").getValue().toString());
                    oldImageUri = imageUri;
                    Picasso.with(PlaceProfile.this).load(imageUri).fit().into(Photo);
                }
                Name.setText(OldName);
                Address.setText(OldAddress);
                Brandtxt.setText("Brand: " + OldBrand);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GetPhoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    imageUri = result;
                    Picasso.with(PlaceProfile.this).load(result).fit().into(Photo);
                }
            }
        );
        Upload.setOnClickListener(view->{
            GetPhoto.launch("image/*");
        });

        Save.setOnClickListener(view->{
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(PlaceProfile.this, "Saving in Progress", Toast.LENGTH_SHORT).show();
            } else {
                String NameText = Name.getText().toString();
                String AddressText = Address.getText().toString();
                String BrandText = Brand.getSelectedItem().toString();
                if (Address.getText() == null || AddressText.matches("") || Name.getText() == null || NameText.matches("")) {
                    Toast.makeText(PlaceProfile.this, "Fields must not be blank", Toast.LENGTH_SHORT).show();
                } else if (!OldPassword.matches(Confirm.getText().toString())) {
                    Toast.makeText(PlaceProfile.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                } else {
                    if (!OldName.matches(NameText.trim())) {
                        currentUser.child("placeName").setValue(NameText);
                    }
                    if (!OldAddress.matches(AddressText.trim())) {
                        currentUser.child("placeAdd").setValue(AddressText);
                        Geocoder geocoder = new Geocoder(PlaceProfile.this, Locale.getDefault());
                        List<Address> address1 = null;
                        try {
                            address1 = geocoder.getFromLocationName(AddressText, 1);
                            if (address1.size() > 0) {
                                currentUser.child("placeLat").setValue(address1.get(0).getLatitude());
                                currentUser.child("placeLng").setValue(address1.get(0).getLongitude());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!OldBrand.matches(BrandText) && !BrandText.matches("None")){
                        currentUser.child("brand").setValue(BrandText);
                    }
                    if (imageUri == null || (oldImageUri != null && oldImageUri.equals(imageUri)) ) {
                        Toast.makeText(PlaceProfile.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(this.getIntent());
                    } else {
                        UploadFile();
                        Toast.makeText(PlaceProfile.this, "Profile Saving, Please wait.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        Back.setOnClickListener(view->{
            finish();
        });

    }

    private String GetFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadFile(){
        if (imageUri != null){
            StorageReference fileRef = photoRef.child(System.currentTimeMillis()+"."+GetFileExtension(imageUri));
            mUploadTask = fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (oldImageUri != null){
                                        StorageReference currentPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri.toString());
                                        currentPhotoRef.delete();
                                    }
                                    currentUser.child("placePhoto").setValue(uri.toString());
                                    Toast.makeText(PlaceProfile.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(PlaceProfile.this.getIntent());
                                }
                            });
                        }
                    });
        }
    };

}