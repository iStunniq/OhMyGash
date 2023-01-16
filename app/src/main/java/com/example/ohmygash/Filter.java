package com.example.ohmygash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Filter extends AppCompatActivity {

    TextView Title,BrandText,StatusText;
    EditText Name,Radius;
    Button Distance,Price,Back;
    Spinner Brand,Status;
    String SelectedBrand,SelectedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Intent currentintent = getIntent();
        String accType = currentintent.getStringExtra("AccType");

        Title = findViewById(R.id.FilterTitle);
        Name = findViewById(R.id.FilterName);
        Distance = findViewById(R.id.FilterDistance);
        Price = findViewById(R.id.FilterCheapest);
        Back = findViewById(R.id.ReturnToFind);
        Radius = findViewById(R.id.FilterDistanceAmount);
        BrandText = findViewById(R.id.FilterBrandText);
        Brand = findViewById(R.id.BrandSpinner);
        StatusText = findViewById(R.id.StatusText);
        Status = findViewById(R.id.StatusSpinner);

        Back.setOnClickListener(view->{
            finish();
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.station_array, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
        Brand.setAdapter(adapter);

        Brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SelectedBrand = Brand.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (accType.matches("Requests") || accType.matches("AllAccounts"))
        {
//            Radius.setVisibility(View.INVISIBLE);
//            Radius.setClickable(false);
//            Radius.setEnabled(false);
            Price.setVisibility(View.GONE);
            Distance.setText("Filter");
            Status.setVisibility(View.VISIBLE);
            StatusText.setVisibility(View.VISIBLE);

            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.verification_array, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
            adapter2.setDropDownViewResource(androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item);
            Status.setAdapter(adapter2);

            Status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SelectedStatus = Status.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else if (accType.matches("Autoshop")) {
            Price.setVisibility(View.GONE);
            Brand.setVisibility(View.INVISIBLE);
            Brand.setClickable(false);
            Brand.setEnabled(false);
            BrandText.setVisibility(View.INVISIBLE);
        }

        Distance.setOnClickListener(view->{
            FilterPage(Name.getText().toString(),"Distance",accType,Radius.getText().toString(),SelectedBrand);
        });
        Price.setOnClickListener(view->{
            FilterPage(Name.getText().toString(),"Price",accType,Radius.getText().toString(),SelectedBrand);
        });

        if (accType.matches("Autoshop")) {
            Price.setVisibility(View.GONE);
        };

    }

    public void FilterPage(String name, String filterby, String acctype, String radius, String brand) {
        Intent intent = new Intent(Filter.this, LocatePlace.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("AccountTypeToLocate", acctype);
        if (name.length() > 0) {
            intent.putExtra("FilterName", name);
        }
        if (brand.length() > 0 && !brand.matches("None")){
            intent.putExtra("FilterBrand",brand);
        }
        if (SelectedStatus != null && SelectedStatus.length() > 0 && !SelectedStatus.matches("None")){
            intent.putExtra("FilterStatus",SelectedStatus);
        }
        if (radius.length()>0){
            intent.putExtra("Radius",Integer.valueOf(radius));
        }
        intent.putExtra("FilterType",filterby);
        startActivity(intent);
    }
}