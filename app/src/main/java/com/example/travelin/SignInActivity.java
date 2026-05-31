package com.example.travelin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class SignInActivity extends Activity {
    private static final String PREFS_NAME = "travelin_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_AR = "ar";

    private TextView languageText;
    private TextView titleText;
    private TextView subtitleText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView forgotPasswordText;
    private Button signInButton;
    private TextView socialLabelText;
    private TextView noAccountText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        languageText = findViewById(R.id.txt_language);
        titleText = findViewById(R.id.txt_sign_in_title);
        subtitleText = findViewById(R.id.txt_sign_in_subtitle);
        emailEditText = findViewById(R.id.edt_email);
        passwordEditText = findViewById(R.id.edt_password);
        forgotPasswordText = findViewById(R.id.txt_forgot_password);
        signInButton = findViewById(R.id.btn_sign_in);
        socialLabelText = findViewById(R.id.txt_social_label);
        noAccountText = findViewById(R.id.txt_no_account);
        signUpButton = findViewById(R.id.btn_go_sign_up);

        applyLanguage(getSavedLanguage());

        languageText.setOnClickListener(v -> showLanguageMenu());
        signInButton.setOnClickListener(v ->
                Toast.makeText(this, "Sign in clicked", Toast.LENGTH_SHORT).show());
        signUpButton.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        forgotPasswordText.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show());

        setSocialToast(R.id.btn_google, "Google clicked");
        setSocialToast(R.id.btn_facebook, "Facebook clicked");
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
            setTitleWithPrimaryWord("Connecte-toi avec Travel.", "Travel");
            subtitleText.setText("Découvre le monde à chaque\nconnexion");
            emailEditText.setHint("E-mail ou numéro de téléphone");
            passwordEditText.setHint("Mot de passe");
            forgotPasswordText.setText("Mot de passe oublié ?");
            signInButton.setText("Se connecter");
            socialLabelText.setText("ou se connecter avec");
            noAccountText.setText("Je n'ai pas de compte ?");
            signUpButton.setText("Créer un compte");
        } else if (isArabic) {
            languageText.setText("العربية v");
            setTitleWithPrimaryWord("سجّل دخولك مع Travelin", "Travelin");
            subtitleText.setText("اكتشف العالم مع كل\nتسجيل دخول");
            emailEditText.setHint("البريد الإلكتروني أو رقم الهاتف");
            passwordEditText.setHint("كلمة المرور");
            forgotPasswordText.setText("هل نسيت كلمة المرور؟");
            signInButton.setText("تسجيل الدخول");
            socialLabelText.setText("أو سجّل الدخول باستخدام");
            noAccountText.setText("ليس لديك حساب؟");
            signUpButton.setText("إنشاء حساب");
        } else {
            languageText.setText("English v");
            setTitleWithPrimaryWord("Let's Travel you in.", "Travel");
            subtitleText.setText("Discover the World with Every\nSign In");
            emailEditText.setHint("Email or Phone Number");
            passwordEditText.setHint("Password");
            forgotPasswordText.setText("Forgot password?");
            signInButton.setText("Sign In");
            socialLabelText.setText("or sign in with");
            noAccountText.setText("I don't have an account?");
            signUpButton.setText("Sign Up");
        }
    }

    private void setTitleWithPrimaryWord(String text, String word) {
        SpannableString styledText = new SpannableString(text);
        int start = text.indexOf(word);
        if (start >= 0) {
            styledText.setSpan(new ForegroundColorSpan(Color.parseColor("#007A8C")),
                    start, start + word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        titleText.setText(styledText);
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

    private void setSocialToast(int viewId, String message) {
        findViewById(viewId).setOnClickListener(v ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
