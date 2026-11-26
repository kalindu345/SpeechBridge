package lk.kalindu.speechbridge.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;

import lk.kalindu.speechbridge.activity.MapPickerActivity;
import lk.kalindu.speechbridge.activity.RoleSelectionActivity; // <- Add this import
import lk.kalindu.speechbridge.databinding.FragmentTherapistScheduleBinding;
import lk.kalindu.speechbridge.model.Session;
import lk.kalindu.speechbridge.network.ApiClient;

public class TherapistScheduleFragment extends Fragment {

    private FragmentTherapistScheduleBinding binding;
    private Calendar selectedDate;
    private LatLng pinnedLocation; // From map picker

    private static final int REQUEST_MAP_PICKER = 1001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTherapistScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSessionTypeDropdown();
        setupDatePicker();
        setupTimePicker();
        handleTypeChange();
        handleMapPicker();
        handleCreateSession();
    }

    private void setupSessionTypeDropdown() {
        String[] types = {"online", "physical"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                types
        );
        binding.inputSessionType.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.inputDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(
                    getContext(),
                    (view, year, month, day) -> {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day);
                        binding.inputDate.setText(day + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupTimePicker() {
        binding.inputTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(
                    getContext(),
                    (view, hour, minute) -> {
                        String amPm = (hour >= 12) ? "PM" : "AM";
                        int hour12 = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);
                        binding.inputTime.setText(String.format("%02d:%02d %s", hour12, minute, amPm));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            ).show();
        });
    }

    private void handleTypeChange() {
        binding.inputSessionType.setOnItemClickListener((parent, view, position, id) -> {
            String type = binding.inputSessionType.getText().toString();
            binding.layoutMeetingLink.setVisibility("online".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);
            binding.layoutLocation.setVisibility("physical".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);
        });
    }

    private void handleMapPicker() {
        binding.btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapPickerActivity.class);
            startActivityForResult(intent, REQUEST_MAP_PICKER);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP_PICKER && resultCode == getActivity().RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);
            String name = data.getStringExtra("name");

            pinnedLocation = new LatLng(lat, lng);
            binding.txtSelectedLocation.setText(name != null ? name : "Pinned: " + lat + ", " + lng);
        }
    }

    private void handleCreateSession() {
        binding.btnCreateSession.setOnClickListener(v -> {
            String type = binding.inputSessionType.getText().toString().trim();
            String startTime = binding.inputTime.getText().toString().trim();
            String priceStr = binding.inputPrice.getText().toString().trim();
            String location = binding.inputLocation.getText().toString().trim();
            String meetingLink = binding.inputMeetingLink.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                showToast("Select session type");
                return;
            }
            if (selectedDate == null) {
                showToast("Select date");
                return;
            }
            if (TextUtils.isEmpty(startTime)) {
                showToast("Select start time");
                return;
            }
            if (TextUtils.isEmpty(priceStr)) {
                showToast("Enter price");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                showToast("Invalid price");
                return;
            }

            if ("physical".equalsIgnoreCase(type)) {
                if (TextUtils.isEmpty(location)) {
                    showToast("Enter location");
                    return;
                }
                if (pinnedLocation == null) {
                    showToast("Pick location from map");
                    return;
                }
            }

            if ("online".equalsIgnoreCase(type) && TextUtils.isEmpty(meetingLink)) {
                showToast("Enter meeting link");
                return;
            }

            String therapistId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            GeoPoint geoPoint = pinnedLocation != null ? new GeoPoint(pinnedLocation.latitude, pinnedLocation.longitude) : null;

            Session session = Session.builder()
                    .therapistId(therapistId)
                    .therapistName("")
                    .patientId("")
                    .type(type)
                    .startTime(startTime)
                    .location(type.equals("physical") ? location : "")
                    .pinnedLocation(geoPoint)
                    .meetingLink(type.equals("online") ? meetingLink : "")
                    .price(price)
                    .status("available")
                    .date(new Timestamp(selectedDate.getTime()))
                    .build();

            // API call
            ApiClient.createSession(session, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(Session session) {
                    requireActivity().runOnUiThread(() -> {
                        showToast("Session Created!");
                        clearForm();

                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    requireActivity().runOnUiThread(() -> showToast("Error: " + errorMessage));
                }
            });
        });
    }

    private void clearForm() {
        binding.inputSessionType.setText("");
        binding.inputDate.setText("");
        binding.inputTime.setText("");
        binding.inputPrice.setText("");
        binding.inputLocation.setText("");
        binding.inputMeetingLink.setText("");
        binding.txtSelectedLocation.setText("No location selected");

        binding.layoutLocation.setVisibility(View.GONE);
        binding.layoutMeetingLink.setVisibility(View.GONE);

        selectedDate = null;
        pinnedLocation = null;
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}