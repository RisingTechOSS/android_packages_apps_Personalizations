package com.rising.settings.fragments.ui;

import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.SharedPreferences;
import android.os.Handler;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

public class LockScreenClockPicker extends Fragment {

    private ViewPager2 viewPager;
    private String[] customFonts;
    private int currentPosition = 0;
    private SharedPreferences sharedPreferences;
    private ContentResolver resolver;
    private Handler handler;
    private boolean isViewPagerSwiped = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lockscreen_clock_font_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context mContext = getActivity().getApplicationContext();
        resolver = mContext.getContentResolver();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        handler = new Handler();

        viewPager = view.findViewById(R.id.viewPager);
        customFonts = getResources().getStringArray(R.array.custom_font_entries);

        LockScreenClockPickerPagerAdapter pagerAdapter = new LockScreenClockPickerPagerAdapter(customFonts, resolver, mContext);
        viewPager.setAdapter(pagerAdapter);

        // Add a PageTransformer for the lock screen preview effect
        CompositePageTransformer pageTransformer = new CompositePageTransformer();
        pageTransformer.addTransformer(new LockScreenPageTransformer());
        pageTransformer.addTransformer(new MarginPageTransformer(32));
        viewPager.setPageTransformer(pageTransformer);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean isScrolling = false;

            @Override
            public void onPageScrollStateChanged(int state) {
                isScrolling = state != ViewPager2.SCROLL_STATE_IDLE;
                if (!isScrolling && currentPosition != -1) {
                    viewPager.setCurrentItem(currentPosition, false);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (isScrolling) {
                    currentPosition = position;
                }
                isViewPagerSwiped = (positionOffsetPixels != 0);
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                viewPager.setCurrentItem(currentPosition, false);
                String fontName = customFonts[currentPosition];
                sharedPreferences.edit().putString(Settings.Secure.KG_FONT_TYPE, fontName).apply();

                setPreviewFont(fontName);
            }
        });

        LockscreenClockSettingsPreferenceFragment preferenceFragment = new LockscreenClockSettingsPreferenceFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.preferenceContainer, preferenceFragment)
                .commit();

        String selectedFont = sharedPreferences.getString(Settings.Secure.KG_FONT_TYPE, "sans-serif");

        for (int i = 0; i < customFonts.length; i++) {
            if (customFonts[i].equals(selectedFont)) {
                currentPosition = i;
                viewPager.setCurrentItem(currentPosition, false);
                break;
            }
        }
        
        Button applyButton = view.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText;
                if (isViewPagerSwiped) {
                    buttonText = getString(R.string.qs_apply_change_button_title);
                } else {
                    buttonText = getString(R.string.swipe_the_clock);
                }
                applyButton.setText(buttonText);
                String selectedFont = customFonts[currentPosition];
                setPreviewFont(selectedFont);
                handler.removeCallbacks(updateSettingsProviderRunnable);
                handler.postDelayed(updateSettingsProviderRunnable, 500);
            }
        });
        
        setPreviewFont(customFonts[currentPosition]);
    }

    private void updateSettingsProvider(String fontName) {
        Settings.Secure.putStringForUser(resolver, Settings.Secure.KG_FONT_TYPE, fontName, UserHandle.USER_CURRENT);
    }

    private Runnable updateSettingsProviderRunnable = new Runnable() {
        @Override
        public void run() {
            String fontName = customFonts[currentPosition];
            updateSettingsProvider(fontName);
        }
    };

    private void setPreviewFont(String fontName) {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(currentPosition);

        if (viewHolder instanceof LockScreenClockPickerPagerAdapter.FontViewHolder) {
            LockScreenClockPickerPagerAdapter.FontViewHolder fontViewHolder =
                    (LockScreenClockPickerPagerAdapter.FontViewHolder) viewHolder;
            fontViewHolder.setClockPreviewFont(fontName);
        }

        String buttonText;
        if (isViewPagerSwiped) {
            buttonText = getString(R.string.qs_apply_change_button_title);
        } else {
            buttonText = getString(R.string.swipe_the_clock);
        }
        Button applyButton = requireView().findViewById(R.id.applyButton);
        applyButton.setText(buttonText);
    }

    private static class LockScreenPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        private final float pageWidthRatio = 0.75f;
        private final float pageHeightRatio = 0.75f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float verticalMargin = page.getHeight() * (1 - scaleFactor) / 2;
            float horizontalMargin = page.getWidth() * (1 - scaleFactor) / 2;

            if (position < 0) {
                page.setTranslationX(horizontalMargin - verticalMargin / 2);
            } else {
                page.setTranslationX(-horizontalMargin + verticalMargin / 2);
            }

            float targetWidth = page.getWidth() * scaleFactor * pageWidthRatio;
            float targetHeight = page.getHeight() * scaleFactor * pageHeightRatio;
            page.setScaleX(targetWidth / page.getWidth());
            page.setScaleY(targetHeight / page.getHeight());

            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

    public static class LockscreenClockSettingsPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.rising_settings_lockscreen_clock, rootKey);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String title = getString(R.string.theme_customization_clock_font_title);
            getActivity().setTitle(title);
        }
    }
}
