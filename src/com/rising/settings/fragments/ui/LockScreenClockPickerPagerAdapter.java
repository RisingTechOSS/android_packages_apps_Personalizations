package com.rising.settings.fragments.ui;

import android.content.ContentResolver;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.settings.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LockScreenClockPickerPagerAdapter extends RecyclerView.Adapter<LockScreenClockPickerPagerAdapter.FontViewHolder> {

    private String[] fontNames;
    private ContentResolver resolver;
    private Context context;
    private int selectedPosition = 0;

    public LockScreenClockPickerPagerAdapter(String[] fontNames, ContentResolver resolver, Context context) {
        this.fontNames = fontNames;
        this.resolver = resolver;
        this.context = context;
    }

    @NonNull
    @Override
    public FontViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_font_preview, parent, false);
        return new FontViewHolder(itemView, resolver, context);
    }

    @Override
    public void onBindViewHolder(@NonNull FontViewHolder holder, int position) {
        String fontName = fontNames[position];
        holder.bind(fontName);
    }

    @Override
    public int getItemCount() {
        return fontNames.length;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    static class FontViewHolder extends RecyclerView.ViewHolder {

        private TextView fontTextView;
        private TextClock clockTextView;
        private ContentResolver resolver;
        private int defaultColor;
        boolean clockColorEnabled;
        int clockColor;
        float bigClockTextSize;

        public FontViewHolder(@NonNull View itemView, ContentResolver resolver, Context context) {
            super(itemView);
            clockTextView = itemView.findViewById(R.id.previewTextClock);
            fontTextView = itemView.findViewById(R.id.previewTextClockName);
            this.resolver = resolver;
            defaultColor = ContextCompat.getColor(context, android.R.color.system_accent1_100);
        }

        public void bind(String fontName) {
            Typeface typeface = Typeface.create(fontName, Typeface.NORMAL);
            clockTextView.setTypeface(typeface);
            fontTextView.setText(fontName);
            fontTextView.setTextColor(Color.WHITE);
            try {
                clockColorEnabled = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, UserHandle.USER_CURRENT) == 1;
                clockColor = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_CUSTOM_CLOCK_COLOR, UserHandle.USER_CURRENT);
                bigClockTextSize = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_LARGE_CLOCK_TEXT_SIZE, UserHandle.USER_CURRENT);
                clockTextView.setTextColor(clockColorEnabled ? clockColor : defaultColor);
                clockTextView.setTextSize(bigClockTextSize / 2.5f); // ratio based on default text size (72sp)
             } catch (SettingNotFoundException e) {}
        }
        
        public void setClockPreviewFont(String fontName) {
             Typeface typeface = Typeface.create(fontName, Typeface.NORMAL);
             clockTextView.setTypeface(typeface);
             try {
                 clockColorEnabled = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, UserHandle.USER_CURRENT) == 1;
                 clockColor = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_CUSTOM_CLOCK_COLOR, UserHandle.USER_CURRENT);
                 bigClockTextSize = Settings.Secure.getIntForUser(resolver, Settings.Secure.KG_LARGE_CLOCK_TEXT_SIZE, UserHandle.USER_CURRENT);
                 clockTextView.setTextColor(clockColorEnabled ? clockColor : defaultColor);
                 clockTextView.setTextSize(bigClockTextSize / 2.5f); // ratio based on default text size (72sp)
              } catch (SettingNotFoundException e) {}
        }
    }
}

