package lk.kalindu.speechbridge.fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lk.kalindu.speechbridge.R;

public class TherapistLocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_LOCATION = "arg_location";
    private static final String ARG_LAT = "arg_lat";
    private static final String ARG_LNG = "arg_lng";
    private static final String ARG_HAS_LATLNG = "hasLatLng";

    private String location;
    private Double latitude, longitude;
    private boolean hasLatLng;

    private MapView mapView;
    private GoogleMap googleMap;

    public static TherapistLocationFragment newInstance(String location, Double lat, Double lng) {
        TherapistLocationFragment fragment = new TherapistLocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location);
        if (lat != null && lng != null) {
            args.putDouble(ARG_LAT, lat);
            args.putDouble(ARG_LNG, lng);
            args.putBoolean(ARG_HAS_LATLNG, true);
        } else {
            args.putBoolean(ARG_HAS_LATLNG, false);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location = getArguments().getString(ARG_LOCATION);
            hasLatLng = getArguments().getBoolean(ARG_HAS_LATLNG, false);
            if (hasLatLng) {
                latitude = getArguments().getDouble(ARG_LAT);
                longitude = getArguments().getDouble(ARG_LNG);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_therapist_location, container, false);

        mapView = view.findViewById(R.id.mapView);
        Button okButton = view.findViewById(R.id.okButton);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        okButton.setOnClickListener(v -> {
            // Go back to previous fragment (session list)
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng target = null;

        if (hasLatLng && latitude != null && longitude != null) {
            target = new LatLng(latitude, longitude);
        } else if (location != null && !location.isEmpty()) {
            // Geocode location /address
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(location, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    target = new LatLng(addr.getLatitude(), addr.getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to find location on map", Toast.LENGTH_SHORT).show();
            }
        }

        if (target != null) {
            googleMap.addMarker(new MarkerOptions().position(target).title(location != null ? location : "Therapy Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f));
        } else {
            Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}