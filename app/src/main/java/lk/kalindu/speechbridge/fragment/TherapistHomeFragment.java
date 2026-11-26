package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.adapter.ImageSliderAdapter;

public class TherapistHomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    public TherapistHomeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_therapist_home, container, false);

        viewPager = view.findViewById(R.id.therapistImageSlider);


        int[] images = {
                R.drawable.slider1,
                R.drawable.slider2,
                R.drawable.slider3,

        };

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // 3 seconds
            }
        });

        return view;
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null) {
                int nextItem = viewPager.getCurrentItem() + 1;

                // Loop back to first
                if (nextItem >= viewPager.getAdapter().getItemCount()) {
                    nextItem = 0;
                }

                viewPager.setCurrentItem(nextItem, true);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}