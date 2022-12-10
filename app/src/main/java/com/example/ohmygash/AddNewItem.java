package com.example.ohmygash;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddNewItem extends AppCompatActivity {

    private ActivityResultLauncher<String> GetPhoto;
    private Uri imageUri, oldImageUri;

    private Button Return, Add, Delete, Upload;
    private TextView Title;
    private EditText Name, Description, Price;
    private RadioGroup itemType;
    private RadioButton SelectedItem;
    private ImageView Photo;
    DatabaseReference Item,NewItem;
    private StorageReference photoRef = FirebaseStorage.getInstance().getReference("ItemsPhotos");
    private StorageTask mUploadTask;

    String ItemId;
    String ItemUser;
    String ItemType;

    FirebaseAuth FBAuth;
    FirebaseUser FBUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);

        Intent ThisIntent = getIntent();
        ItemId = ThisIntent.getStringExtra("ItemId");
        ItemUser = ThisIntent.getStringExtra("ItemUser");
        ItemType = ThisIntent.getStringExtra("ItemType");


        Return = findViewById(R.id.ReturnNewItem);
        itemType = findViewById(R.id.ItemTypeRadioGroup);
        SelectedItem = findViewById(itemType.getCheckedRadioButtonId());
        Add = findViewById(R.id.FinishAddNewItem);
        Name = findViewById(R.id.AddNewItemName);
        Description = findViewById(R.id.AddNewItemDescription);
        Price = findViewById(R.id.AddNewItemPrice);
        Title = findViewById(R.id.ManageItemTitle);
        Delete = findViewById(R.id.DeleteItemButton);
        Upload = findViewById(R.id.UploadItemPhoto);
        Photo = findViewById(R.id.ItemPhoto);

        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Return.setOnClickListener(view -> {
            finish();
        });

        GetPhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imageUri = result;
                        Picasso.with(AddNewItem.this).load(result).fit().into(Photo);
                    }
                }
        );

        Upload.setOnClickListener(view->{
            GetPhoto.launch("image/*");
        });



        itemType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                SelectedItem = findViewById(i);
                if (SelectedItem.getText().toString().matches("Gasoline")) {
                    Price.setHint("Item Price in Pesos per Liter");
                } else {
                    Price.setHint("Item Price in Pesos (0 = 'Unset')");
                }
            }
        });

        if (ItemId == null) {
            databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(FBUser.getUid()).child("accType").getValue().toString().matches("Autoshop")) {
                        itemType.check(R.id.ProductRadioButton);
                        findViewById(R.id.GasolineRadioButton).setClickable(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Delete.setVisibility(View.GONE);
            Add.setOnClickListener(view -> {
                final String nameTxt = Name.getText().toString().trim();
                final String descriptionTxt = Description.getText().toString().trim();
                final String priceTxt = Price.getText().toString().trim();

                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(AddNewItem.this, "Saving in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    if (nameTxt.isEmpty() || nameTxt == "" || priceTxt.isEmpty() || priceTxt == "") {
                        Toast.makeText(AddNewItem.this, "Please Fill in All Entries", Toast.LENGTH_LONG).show();
                    } else {
                        DatabaseReference User = databaseReference.child(SelectedItem.getText().toString()).child(FBUser.getUid());
                        String ProductId = User.push().getKey();
                        NewItem = User.child(ProductId);
                        NewItem.child("Name").setValue(nameTxt);
                        if (descriptionTxt == null || descriptionTxt.isEmpty()){
                            NewItem.child("Description").setValue("No Description");
                        }else {
                            NewItem.child("Description").setValue(descriptionTxt);
                        }
                        NewItem.child("Price").setValue(priceTxt);
                        if (imageUri == null || (oldImageUri != null && oldImageUri.equals(imageUri)) ) {
                            Toast.makeText(AddNewItem.this, "Successfully added new " + SelectedItem.getText().toString(), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AddNewItem.this, ManageInventory.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            UploadFile();
                            Toast.makeText(AddNewItem.this, "Adding New " + SelectedItem.getText().toString() + ", Please wait.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Title.setText("Edit " + ItemType);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DataSnapshot item = snapshot.child(ItemType).child(ItemUser).child(ItemId);
                    Name.setText(item.child("Name").getValue().toString());
                    Description.setText(item.child("Description").getValue().toString());
                    Price.setText(item.child("Price").getValue().toString());
                    if (item.child("Photo").getValue() != null) {
                        imageUri = Uri.parse(item.child("Photo").getValue().toString());
                        oldImageUri = imageUri;
                        Picasso.with(AddNewItem.this).load(imageUri).fit().into(Photo);
                    }
                    itemType.setVisibility(View.GONE);
                    findViewById(R.id.ItemTypeTextView).setVisibility(View.GONE);
                    Add.setText("Update");

                    Add.setOnClickListener(view -> {
                        if (mUploadTask != null && mUploadTask.isInProgress()) {
                            Toast.makeText(AddNewItem.this, "Saving in Progress", Toast.LENGTH_SHORT).show();
                        } else {
                            Item = FirebaseDatabase.getInstance().getReference().child(ItemType).child(ItemUser).child(ItemId);
                            String nameTxt = Name.getText().toString();
                            String descriptionTxt = Name.getText().toString();
                            String priceTxt = Price.getText().toString();
                            if (nameTxt.isEmpty() || nameTxt == "" || priceTxt.isEmpty() || priceTxt == "") {
                                Toast.makeText(AddNewItem.this, "Please Fill in All Entries", Toast.LENGTH_LONG).show();
                            } else {
                                Item.child("Name").setValue(Name.getText().toString());
                                if (descriptionTxt == null || descriptionTxt.isEmpty()){
                                    Item.child("Description").setValue("No Description");;
                                }else {
                                    Item.child("Description").setValue(Description.getText().toString());
                                }
                                Item.child("Price").setValue(Price.getText().toString());
                            }
                            if (nameTxt.isEmpty() || nameTxt == "" || priceTxt.isEmpty() || priceTxt == "") {
                                Toast.makeText(AddNewItem.this, "Please Fill in All Entries", Toast.LENGTH_LONG).show();
                            } else {
                                if (imageUri == null || (oldImageUri != null && oldImageUri.equals(imageUri))) {
                                    Toast.makeText(AddNewItem.this, "Updated " + ItemType, Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(AddNewItem.this, ManageInventory.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                } else {
                                    UploadFile();
                                    Toast.makeText(AddNewItem.this, "Updating Photo, Please wait.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });


                    Delete.setOnClickListener(view -> {
                        DatabaseReference Item = FirebaseDatabase.getInstance().getReference().child(ItemType).child(ItemUser).child(ItemId);
                        Object itemuri = snapshot.child(ItemType).child(ItemUser).child(ItemId).child("Photo").getValue();
                        if (itemuri != null){
                            FirebaseStorage.getInstance().getReferenceFromUrl(itemuri.toString()).delete();
                        }
                        Item.removeValue();
                        Toast.makeText(AddNewItem.this, "Deleted " + ItemType, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddNewItem.this, ManageInventory.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private String GetFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadFile() {
        if (imageUri != null) {
            StorageReference fileRef = photoRef.child(System.currentTimeMillis() + "." + GetFileExtension(imageUri));

//            Bitmap Fullsize = null;
//            try {
//                Fullsize = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageUri));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            Bitmap ReducedSize = ImageResizer.reduceBitmapSize(Fullsize,2000000);
//
//            byte[] ReducedFile = ImageResizer.GetFile(ReducedSize);

            mUploadTask = fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (oldImageUri != null) {
                                        StorageReference currentPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri.toString());
                                        currentPhotoRef.delete();
                                    }

                                    if (ItemType!=null) {
                                        Item.child("Photo").setValue(uri.toString());
                                        Toast.makeText(AddNewItem.this, "Updated " + ItemType, Toast.LENGTH_SHORT).show();
                                    } else {
                                        NewItem.child("Photo").setValue(uri.toString());
                                        Toast.makeText(AddNewItem.this, "Successfully added new " + SelectedItem.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    Intent intent = new Intent(AddNewItem.this, ManageInventory.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
        }
    }


}