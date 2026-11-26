package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

import lk.kalindu.speechbridge.R;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCNgFhvh6Wmcgi9ZpjgL9PZHlDnGeSfSiw");
        }

        // Setup Google AutoComplete
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            );

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null && mMap != null) {
                        selectedLatLng = place.getLatLng();


                        if (selectedMarker != null) selectedMarker.remove();

                        selectedMarker = mMap.addMarker(new MarkerOptions()
                                .position(selectedLatLng)
                                .title(place.getName()));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(MapPickerActivity.this,
                            "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Load google map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Confirm button
        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent intent = new Intent();
                intent.putExtra("lat", selectedLatLng.latitude);
                intent.putExtra("lng", selectedLatLng.longitude);
                intent.putExtra("name", selectedMarker != null ? selectedMarker.getTitle() : "Pinned Location");
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default location
        LatLng defaultLocation = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        // Manual tap to place marker
        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;

            // Remove previous marker
            if (selectedMarker != null) selectedMarker.remove();

            selectedMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Pinned Location"));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        });
    }
}