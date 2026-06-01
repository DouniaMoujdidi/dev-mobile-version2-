package com.example.travelin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class SignInActivity extends Activity {
    private static final String PREFS_NAME = "travelin_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_AR = "ar";
    private static final int RC_GOOGLE_SIGN_IN = 1001;

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
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        bindViews();
        auth = getFirebaseAuthOrNull();
        setupGoogleSignIn();
        setupFacebookSignIn();
        applyLanguage(getSavedLanguage());

        languageText.setOnClickListener(v -> showLanguageMenu());
        signInButton.setOnClickListener(v -> signInWithEmail());
        signUpButton.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        forgotPasswordText.setOnClickListener(v -> sendPasswordResetEmail());
        findViewById(R.id.btn_google).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.btn_facebook).setOnClickListener(v -> signInWithFacebook());
    }

    private void bindViews() {
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
    }

    private FirebaseAuth getFirebaseAuthOrNull() {
        try {
            FirebaseApp.initializeApp(this);
            return FirebaseAuth.getInstance();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private void setupGoogleSignIn() {
        String webClientId = getString(R.string.default_web_client_id);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
    }

    private void setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(SignInActivity.this, "Facebook sign in cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SignInActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signInWithEmail() {
        if (!isFirebaseReady()) {
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (!validateEmailAndPassword(email, password)) {
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        showAuthError(task.getException());
                    }
                });
    }

    private void sendPasswordResetEmail() {
        if (!isFirebaseReady()) {
            return;
        }

        String email = emailEditText.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email first");
            emailEditText.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        showAuthError(task.getException());
                    }
                });
    }

    private void signInWithGoogle() {
        if (!isFirebaseReady() || !isGoogleConfigured()) {
            return;
        }
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN);
    }

    private void signInWithFacebook() {
        if (!isFirebaseReady() || !isFacebookConfigured()) {
            return;
        }
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException exception) {
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                    } else {
                        showAuthError(task.getException());
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Facebook sign in successful", Toast.LENGTH_SHORT).show();
                    } else {
                        showAuthError(task.getException());
                    }
                });
    }

    private boolean validateEmailAndPassword(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isFirebaseReady() {
        if (auth != null) {
            return true;
        }
        Toast.makeText(this, "Add app/google-services.json from Firebase Console first", Toast.LENGTH_LONG).show();
        return false;
    }

    private boolean isGoogleConfigured() {
        if (!getString(R.string.default_web_client_id).startsWith("YOUR_")) {
            return true;
        }
        Toast.makeText(this, "Configure default_web_client_id from Firebase", Toast.LENGTH_LONG).show();
        return false;
    }

    private boolean isFacebookConfigured() {
        if (!getString(R.string.facebook_app_id).startsWith("YOUR_")
                && !getString(R.string.facebook_client_token).startsWith("YOUR_")) {
            return true;
        }
        Toast.makeText(this, "Configure Facebook App ID and Client Token", Toast.LENGTH_LONG).show();
        return false;
    }

    private void showAuthError(Exception exception) {
        String message = exception == null ? "Authentication failed" : exception.getMessage();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean isLoading) {
        signInButton.setEnabled(!isLoading);
        signInButton.setText(isLoading ? "Please wait..." : "Sign In");
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
}
