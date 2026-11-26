package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.adapter.HomeImageSliderAdapter;

public class PatientHomeFragment extends Fragment {

    private ViewPager2 imageSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_home, container, false);

        imageSlider = view.findViewById(R.id.homeImageSlider);

        int[] images = {
                R.drawable.slider_image1,
                R.drawable.slider_image2,
                R.drawable.slider_image3
        };

        HomeImageSliderAdapter adapter = new HomeImageSliderAdapter(images);
        imageSlider.setAdapter(adapter);

        return view;
    }
}