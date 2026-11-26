package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import lk.kalindu.speechbridge.R;


public class RoleSelectionActivity extends AppCompatActivity {

    Button btnPatient, btnTherapist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        btnPatient = findViewById(R.id.btnPatient);
        btnTherapist = findViewById(R.id.btnTherapist);

        // Patient button click
        btnPatient.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, PatientSignInActivity.class);
            startActivity(intent);
        });

        //Therapist button Click

        btnTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RoleSelectionActivity.this, TherapistSignInActivity.class);
                startActivity(intent);
            }
        });

    }
}