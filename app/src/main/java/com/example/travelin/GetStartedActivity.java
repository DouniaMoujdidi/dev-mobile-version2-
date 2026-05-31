package com.example.travelin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

public class GetStartedActivity extends Activity {
    private static final String PREFS_NAME = "travelin_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_AR = "ar";

    private TextView languageText;
    private TextView titleText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        languageText = findViewById(R.id.txt_language);
        titleText = findViewById(R.id.txt_get_started_title);
        startButton = findViewById(R.id.btn_start_journey);

        applyLanguage(getSavedLanguage());

        languageText.setOnClickListener(v -> showLanguageMenu());
        startButton.setOnClickListener(v ->
                startActivity(new Intent(GetStartedActivity.this, SignInActivity.class)));
    }

    private void showLanguageMenu() {
        PopupMenu menu = new PopupMenu(this, languageText);
        menu.getMenu().add("English");
        menu.getMenu().add("Français");
        menu.getMenu().add("العربية");
        menu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            if ("Français".equals(selected)) {
                saveLanguage(LANG_FR);
                applyLanguage(LANG_FR);
            } else if ("العربية".equals(selected)) {
                saveLanguage(LANG_AR);
                applyLanguage(LANG_AR);
            } else {
                saveLanguage(LANG_EN);
                applyLanguage(LANG_EN);
            }
            return true;
        });
        menu.show();
    }

    private void applyLanguage(String language) {
        boolean isArabic = LANG_AR.equals(language);
        getWindow().getDecorView().setLayoutDirection(isArabic ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        if (LANG_FR.equals(language)) {
            languageText.setText("Français v");
            titleText.setText("Prêt à explorer\nau-delà des frontières ?");
            startButton.setText("Votre voyage commence ici");
        } else if (isArabic) {
            languageText.setText("العربية v");
            titleText.setText("هل أنت مستعد للاستكشاف\nوراء الحدود؟");
            startButton.setText("رحلتك تبدأ هنا");
        } else {
            languageText.setText("English v");
            titleText.setText("Ready to explore\nbeyond boundaries?");
            startButton.setText("Your Journey Starts Here");
        }
    }

    private String getSavedLanguage() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, LANG_EN);
    }

    private void saveLanguage(String language) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language)
                .apply();
    }
}
