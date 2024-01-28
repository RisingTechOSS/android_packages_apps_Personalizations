package com.android.settings.preferences.lockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class Widgets extends RelativeLayout {

    private static final int[] MAIN_WIDGETS_VIEW_IDS = {
            R.id.main_kg_item_placeholder1,
            R.id.main_kg_item_placeholder2
    };

    private static final int[] WIDGETS_VIEW_IDS = {
            R.id.kg_item_placeholder1,
            R.id.kg_item_placeholder2,
            R.id.kg_item_placeholder3,
            R.id.kg_item_placeholder4
    };

    private Context mContext;
    private View[] mainWidgetViews;
    private View[] widgetViews;
    private int mDarkColor, mDarkColorActive, mLightColor, mLightColorActive;

    public Widgets(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDarkColor = mContext.getResources().getColor(R.color.lockscreen_widget_background_color_dark);
        mLightColor = mContext.getResources().getColor(R.color.lockscreen_widget_background_color_light);
        mDarkColorActive = mContext.getResources().getColor(R.color.lockscreen_widget_active_color_dark);
        mLightColorActive = mContext.getResources().getColor(R.color.lockscreen_widget_active_color_light);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mainWidgetViews = new View[MAIN_WIDGETS_VIEW_IDS.length];
        for (int i = 0; i < MAIN_WIDGETS_VIEW_IDS.length; i++) {
            if (MAIN_WIDGETS_VIEW_IDS[i] != 0) {
                mainWidgetViews[i] = findViewById(MAIN_WIDGETS_VIEW_IDS[i]);
            } else {
                mainWidgetViews[i] = null;
            }
        }
        widgetViews = new View[WIDGETS_VIEW_IDS.length];
        for (int i = 0; i < WIDGETS_VIEW_IDS.length; i++) {
            if (WIDGETS_VIEW_IDS[i] != 0) {
                widgetViews[i] = findViewById(WIDGETS_VIEW_IDS[i]);
            } else {
                widgetViews[i] = null;
            }
        }
        new MyContentObserver(new Handler()).observe();
        updateWidgetViews();
    }

    private void updateWidgetViews() {
        if (mainWidgetViews != null) {
            updateMainWidgetViews(mainWidgetViews, "lockscreen_widgets");
        }
        if (widgetViews != null) {
            updateSecondaryWidgetViews(widgetViews, "lockscreen_widgets_extras");
        }
    }

    private void updateMainWidgetViews(View[] widgetViews, String settingKey) {
        String widgetViewsStyle = Settings.System.getString(mContext.getContentResolver(), settingKey);
        List<String> widgets = parseSettings(widgetViewsStyle);
        if (widgets != null) {
            for (int i = 0; i < widgetViews.length; i++) {
                if (widgetViews[i] != null) {
                    if (i < widgets.size() && !TextUtils.isEmpty(widgets.get(i))) {
                        widgetViews[i].setVisibility(View.VISIBLE);
                        setUpWidgetViews(widgetViews[i], widgets.get(i));
                    } else {
                        widgetViews[i].setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void updateSecondaryWidgetViews(View[] widgetViews, String settingKey) {
        updateMainWidgetViews(widgetViews, settingKey);
    }

    private List<String> parseSettings(String settings) {
        List<String> result = new ArrayList<>();
        if (!TextUtils.isEmpty(settings)) {
            String[] keys = TextUtils.split(settings, ",");
            for (String key : keys) {
                if (!TextUtils.isEmpty(key.trim())) {
                    result.add(key.trim());
                }
            }
        }
        return result;
    }

    private void setUpWidgetViews(View widgetView, String type) {
        switch (type) {
            case "torch":
                setUpWidgetResources(widgetView, R.drawable.ic_flashlight_off, R.string.torch);
                break;
            case "timer":
                setUpWidgetResources(widgetView, R.drawable.ic_alarm, R.string.clock_timer);
                break;
            case "calculator":
                setUpWidgetResources(widgetView, R.drawable.ic_calculator, R.string.calculator);
                break;
            case "media":
                setUpWidgetResources(widgetView, R.drawable.ic_media_play, R.string.controls_media_button_play);
                break;
            case "weather":
                setUpWidgetResources(widgetView, R.drawable.ic_weather, R.string.weather);
                break;
            default:
                break;
        }
    }

    private void setUpWidgetResources(View widgetView, int drawableRes, int stringRes) {
        if (widgetView instanceof ExtendedFloatingActionButton) {
            ExtendedFloatingActionButton efab = (ExtendedFloatingActionButton) widgetView;
            efab.setIcon(getResources().getDrawable(drawableRes));
            efab.setText(getResources().getString(stringRes));
            setButtonActiveState(null, efab, false);
        } else if (widgetView instanceof ImageView) {
            ImageView iv = (ImageView) widgetView;
            iv.setImageResource(drawableRes);
            iv.setBackgroundResource(R.drawable.lockscreen_widget_background_circle);
            setButtonActiveState(iv, null, false);
        }
    }

    private void setButtonActiveState(ImageView iv, ExtendedFloatingActionButton efab, boolean active) {
        int bgTint;
        int tintColor;
        if (active) {
            bgTint = isNightMode() ? mDarkColorActive : mLightColorActive;
            tintColor = isNightMode() ? mDarkColor : mLightColor;
        } else {
            bgTint = isNightMode() ? mDarkColor : mLightColor;
            tintColor = isNightMode() ? mLightColor : mDarkColor;
        }
        if (iv != null) {
            iv.setBackgroundTintList(ColorStateList.valueOf(bgTint));
            iv.setImageTintList(ColorStateList.valueOf(tintColor));
        }
        if (efab != null) {
            efab.setBackgroundTintList(ColorStateList.valueOf(bgTint));
            efab.setIconTint(ColorStateList.valueOf(tintColor));
            efab.setTextColor(tintColor);
        }
    }

    private boolean isNightMode() {
        final Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler h) {
            super(h);
        }

        public void observe() {
            ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(Settings.System.getUriFor("lockscreen_widgets"), false, this);
            cr.registerContentObserver(Settings.System.getUriFor("lockscreen_widgets_extras"), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateWidgetViews();
        }
    }
}
