package com.example.travelin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {
    private static final String PREFS_NAME = "travelin_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_AR = "ar";

    private TextView languageText;
    private TextView titleText;
    private TextView subtitleText;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private TextView socialLabelText;
    private TextView haveAccountText;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        languageText = findViewById(R.id.txt_language);
        titleText = findViewById(R.id.txt_sign_up_title);
        subtitleText = findViewById(R.id.txt_sign_up_subtitle);
        fullNameEditText = findViewById(R.id.edt_full_name);
        emailEditText = findViewById(R.id.edt_email);
        passwordEditText = findViewById(R.id.edt_password);
        confirmPasswordEditText = findViewById(R.id.edt_confirm_password);
        signUpButton = findViewById(R.id.btn_sign_up);
        socialLabelText = findViewById(R.id.txt_social_label);
        haveAccountText = findViewById(R.id.txt_have_account);
        signInButton = findViewById(R.id.btn_go_sign_in);

        applyLanguage(getSavedLanguage());

        languageText.setOnClickListener(v -> showLanguageMenu());
        signUpButton.setOnClickListener(v ->
                Toast.makeText(this, "Sign up clicked", Toast.LENGTH_SHORT).show());
        signInButton.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

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
            titleText.setText("Créer votre compte Travelin");
            subtitleText.setText("Commencez votre voyage avec nous");
            fullNameEditText.setHint("Nom complet");
            emailEditText.setHint("E-mail ou numéro de téléphone");
            passwordEditText.setHint("Mot de passe");
            confirmPasswordEditText.setHint("Confirmer le mot de passe");
            signUpButton.setText("Créer un compte");
            socialLabelText.setText("ou s'inscrire avec");
            haveAccountText.setText("Vous avez déjà un compte ?");
            signInButton.setText("Se connecter");
        } else if (isArabic) {
            languageText.setText("العربية v");
            titleText.setText("أنشئ حسابك في Travelin");
            subtitleText.setText("ابدأ رحلتك معنا");
            fullNameEditText.setHint("الاسم الكامل");
            emailEditText.setHint("البريد الإلكتروني أو رقم الهاتف");
            passwordEditText.setHint("كلمة المرور");
            confirmPasswordEditText.setHint("تأكيد كلمة المرور");
            signUpButton.setText("إنشاء حساب");
            socialLabelText.setText("أو سجّل باستخدام");
            haveAccountText.setText("لديك حساب بالفعل؟");
            signInButton.setText("تسجيل الدخول");
        } else {
            languageText.setText("English v");
            titleText.setText("Create your Travelin account");
            subtitleText.setText("Start your journey with us");
            fullNameEditText.setHint("Full Name");
            emailEditText.setHint("Email or Phone Number");
            passwordEditText.setHint("Password");
            confirmPasswordEditText.setHint("Confirm Password");
            signUpButton.setText("Sign Up");
            socialLabelText.setText("or sign up with");
            haveAccountText.setText("Already have an account?");
            signInButton.setText("Sign In");
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

    private void setSocialToast(int viewId, String message) {
        findViewById(viewId).setOnClickListener(v ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
