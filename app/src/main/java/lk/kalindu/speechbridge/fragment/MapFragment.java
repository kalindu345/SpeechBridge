package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import lk.kalindu.speechbridge.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_LOCATION = "location";
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";

    private String locationName;
    private double latitude;
    private double longitude;

    private MapView mapView;
    private Button btnOk;

    private OnOkClickListener okClickListener;

    public interface OnOkClickListener {
        void onOkClick();
    }

    public void setOnOkClickListener(OnOkClickListener listener) {
        this.okClickListener = listener;
    }

    public static MapFragment newInstance(String location, double lat, double lng) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location);
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        btnOk = view.findViewById(R.id.btnOk);

        if (getArguments() != null) {
            locationName = getArguments().getString(ARG_LOCATION);
            latitude = getArguments().getDouble(ARG_LAT);
            longitude = getArguments().getDouble(ARG_LNG);
        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnOk.setOnClickListener(v -> {
            if (okClickListener != null) {
                okClickListener.onOkClick();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng pos = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(pos).title(locationName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
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
}