package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.Current;

public class Tutorial1 extends AppCompatActivity {

    Button End;
    FloatingActionButton Back,Next;
    ImageView Photo;
    FirebaseAuth FBAuth;
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    TextView Pages;

    int CurrentPhoto = 0;

    int[] Photos = {R.drawable.tutorial101};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial1);
        End = findViewById(R.id.Tutorial1End);
        Back = findViewById(R.id.Tutorial1Back);
        Next = findViewById(R.id.Tutorial1Next);
        Photo = findViewById(R.id.Tutorial1Photo);
        Pages = findViewById(R.id.TutorialPages);

        Intent CurrentIntent = getIntent();
        int PhotoSet = CurrentIntent.getIntExtra("Tutorial",1);

        FBAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FBAuth.getCurrentUser();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot CurrentUser = snapshot.child(user.getUid());
                if (PhotoSet == 1){
                    int[] NewSet = {
                            R.drawable.tutorial101,
                            R.drawable.tutorial102,
                            R.drawable.tutorial103,
                            R.drawable.tutorial104,
                            R.drawable.tutorial105,
                            R.drawable.tutorial106,
                            R.drawable.tutorial107,
                            R.drawable.tutorial108,
                            R.drawable.tutorial109,
                            R.drawable.tutorial110,
                            R.drawable.tutorial111,
                            R.drawable.tutorial112,
                    };
                    Photos = NewSet;
                } else if (PhotoSet == 2) {
                    String Acctype = CurrentUser.child("accType").getValue().toString();
                    if (Acctype.matches("Autoshop") || Acctype.matches("Station")) {
                        int[] NewSet = {
                                R.drawable.tutorial201,
                                R.drawable.tutorial202,
                                R.drawable.tutorial203,
                                R.drawable.tutorial204,
                                R.drawable.tutorial205,
                                R.drawable.tutorial301,
                                R.drawable.tutorial302,
                                R.drawable.tutorial303,
                                R.drawable.tutorial304,
                                R.drawable.tutorial305,
                                R.drawable.tutorial306,
                                R.drawable.tutorial307,
                        };
                        Photos = NewSet;
                    } else {
                        int[] NewSet = {
                                R.drawable.tutorial201,
                                R.drawable.tutorial202,
                                R.drawable.tutorial203,
                                R.drawable.tutorial204,
                                R.drawable.tutorial205,
                        };
                        Photos = NewSet;
                    }
                }
                setphoto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        End.setOnClickListener(view->{
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.child(user.getUid()).getRef().child("Tutorial"+PhotoSet).setValue("Done");
                    if (PhotoSet == 1){
                        Intent intent = new Intent(Tutorial1.this, Map.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (PhotoSet == 2) {
                        Intent intent = new Intent(Tutorial1.this, Menu.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        Back.setOnClickListener(view->{
            CurrentPhoto = CurrentPhoto-1;
            setphoto();
        });

        Next.setOnClickListener(view->{
            CurrentPhoto = CurrentPhoto+1;
            setphoto();
        });

        setphoto();
    }
    public void setphoto(){
        Next.setEnabled(true);
        Back.setEnabled(true);
        End.setText("Skip All");
        if (CurrentPhoto == 0) {
            Back.setEnabled(false);
        }else if (CurrentPhoto == Photos.length-1){
            Next.setEnabled(false);
            End.setText("Finish");
        }
        Pages.setText((CurrentPhoto+1)+"/"+Photos.length);
        Photo.setImageDrawable(getDrawable(Photos[CurrentPhoto]));
//        Picasso.with(Tutorial1.this).load("@drawable/"+Photos[CurrentPhoto]).fit().into(Photo);
    }
}