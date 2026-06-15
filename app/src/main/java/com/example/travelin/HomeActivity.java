package com.example.travelin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int NAV_ACTIVE_COLOR = Color.rgb(0, 158, 158);
    private static final int NAV_INACTIVE_COLOR = Color.rgb(111, 119, 136);

    private TripAdapter tripAdapter;
    private TripDao tripDao;
    private final List<Trip> allTrips = new ArrayList<>();
    private EditText searchInput;
    private TextView noTripsText;
    private FrameLayout rootContainer;
    private LinearLayout navigationBar;
    private FloatingActionButton addTripButton;
    private View notificationsContent;
    private View memoriesContent;
    private View activeMemoryShade;
    private View activeMemoryInfo;
    private Runnable pendingMemoryHide;
    private LinearLayout[] navigationItems;
    private ImageView[] navigationIcons;
    private TextView[] navigationLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        updateUserHeader();
        tripDao = new TripDao(this);
        searchInput = findViewById(R.id.input_search_trip);
        noTripsText = findViewById(R.id.txt_no_trips);

        RecyclerView tripsRecyclerView = findViewById(R.id.recycler_trips);
        tripsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tripsRecyclerView.setNestedScrollingEnabled(true);
        tripAdapter = new TripAdapter(new ArrayList<>(), trip -> {
            Intent intent = new Intent(this, TripDetailActivity.class);
            intent.putExtra(TripDetailActivity.EXTRA_TRIP_ID, trip.getId());
            intent.putExtra(TripDetailActivity.EXTRA_TRIP_NAME, trip.getName());
            intent.putExtra(TripDetailActivity.EXTRA_TRIP_DATES, trip.getDates());
            intent.putExtra(TripDetailActivity.EXTRA_TRIP_IMAGE, trip.getImageResId());
            intent.putExtra(TripDetailActivity.EXTRA_HOTEL_PHONE, trip.getHotelPhone());
            startActivity(intent);
        });
        tripsRecyclerView.setAdapter(tripAdapter);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        replaceBottomNavigation();
    }

    private void replaceBottomNavigation() {
        BottomNavigationView oldNavigation = findViewById(R.id.bottom_navigation);
        FloatingActionButton oldAddButton = findViewById(R.id.fab_add_trip);
        addTripButton = oldAddButton;
        oldNavigation.setVisibility(View.GONE);
        oldAddButton.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams addButtonParams =
                (FrameLayout.LayoutParams) oldAddButton.getLayoutParams();
        addButtonParams.bottomMargin = dp(102);
        oldAddButton.setLayoutParams(addButtonParams);
        oldAddButton.setOnClickListener(view ->
                new TripTypeBottomSheetFragment().show(
                        getSupportFragmentManager(),
                        "TripTypeBottomSheet"
                ));

        ViewGroup content = findViewById(android.R.id.content);
        FrameLayout root = (FrameLayout) content.getChildAt(0);
        rootContainer = root;
        navigationBar = new LinearLayout(this);
        navigationBar.setOrientation(LinearLayout.HORIZONTAL);
        navigationBar.setGravity(Gravity.CENTER);
        navigationBar.setPadding(dp(8), dp(5), dp(8), dp(5));
        navigationBar.setElevation(dp(10));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(22));
        navigationBar.setBackground(background);

        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(76),
                Gravity.BOTTOM
        );
        barParams.setMargins(dp(8), 0, dp(8), dp(7));
        root.addView(navigationBar, barParams);

        String[] labels = {"Accueil", "Memories", "Explorer", "Notifications", "Profil"};
        int[] icons = {
                R.drawable.nav_home,
                R.drawable.nav_memories,
                R.drawable.nav_explorer,
                R.drawable.nav_notification,
                R.drawable.nav_profile
        };

        navigationItems = new LinearLayout[labels.length];
        navigationIcons = new ImageView[labels.length];
        navigationLabels = new TextView[labels.length];

        for (int index = 0; index < labels.length; index++) {
            final int selectedIndex = index;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setClickable(true);
            item.setFocusable(true);
            item.setOnClickListener(view -> onNavigationItemClicked(selectedIndex));

            ImageView icon = new ImageView(this);
            icon.setImageResource(icons[index]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            item.addView(icon, new LinearLayout.LayoutParams(dp(27), dp(27)));

            TextView label = new TextView(this);
            label.setText(labels[index]);
            label.setTextSize(index == 3 ? 9 : 10);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = dp(3);
            item.addView(label, labelParams);

            navigationItems[index] = item;
            navigationIcons[index] = icon;
            navigationLabels[index] = label;
            navigationBar.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }

        selectNavigationItem(0);
    }

    private void onNavigationItemClicked(int index) {
        selectNavigationItem(index);
        if (index == 0) {
            showHomeContent();
            return;
        }
        if (index == 1) {
            showMemoriesContent();
        } else if (index == 2) {
            Toast.makeText(this, "Explorer bientot disponible", Toast.LENGTH_SHORT).show();
        } else if (index == 3) {
            showNotificationsContent();
        } else {
            Toast.makeText(this, "Profil bientot disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationsContent() {
        if (memoriesContent != null) {
            memoriesContent.setVisibility(View.GONE);
        }
        hideActiveMemoryInfo();
        if (notificationsContent == null) {
            notificationsContent = LayoutInflater.from(this)
                    .inflate(R.layout.activity_notifications, rootContainer, false);

            ImageButton backButton = notificationsContent.findViewById(R.id.btn_notifications_back);
            RecyclerView recyclerView = notificationsContent.findViewById(R.id.recycler_notifications);
            backButton.setOnClickListener(view -> {
                selectNavigationItem(0);
                showHomeContent();
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new NotificationAdapter(
                    NotificationsActivity.createNotifications(),
                    item -> Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show()
            ));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            params.bottomMargin = dp(86);
            rootContainer.addView(notificationsContent, params);
        } else {
            notificationsContent.setVisibility(View.VISIBLE);
        }

        addTripButton.setVisibility(View.GONE);
        navigationBar.bringToFront();
    }

    private void showMemoriesContent() {
        if (notificationsContent != null) {
            notificationsContent.setVisibility(View.GONE);
        }
        if (memoriesContent == null) {
            memoriesContent = createMemoriesView();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            params.bottomMargin = dp(76);
            rootContainer.addView(memoriesContent, params);
        } else {
            memoriesContent.setVisibility(View.VISIBLE);
        }

        addTripButton.setVisibility(View.GONE);
        navigationBar.bringToFront();
    }

    private View createMemoriesView() {
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.WHITE);

        FrameLayout header = new FrameLayout(this);
        header.setPadding(dp(12), 0, dp(12), 0);
        header.setBackgroundColor(Color.WHITE);
        screen.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(68)
        ));

        ImageButton backButton = new ImageButton(this);
        backButton.setImageResource(R.drawable.ic_add_trip_back);
        backButton.setImageTintList(ColorStateList.valueOf(Color.rgb(7, 56, 68)));
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true);
        backButton.setBackgroundResource(ripple.resourceId);
        backButton.setPadding(dp(12), dp(12), dp(12), dp(12));
        backButton.setContentDescription("Retour");
        backButton.setOnClickListener(view -> {
            selectNavigationItem(0);
            showHomeContent();
        });
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                dp(48),
                dp(48),
                Gravity.START | Gravity.CENTER_VERTICAL
        );
        header.addView(backButton, backParams);

        TextView headerTitle = new TextView(this);
        headerTitle.setText("Memories");
        headerTitle.setTextColor(Color.rgb(7, 56, 68));
        headerTitle.setTextSize(23);
        headerTitle.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        headerTitle.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        header.addView(headerTitle, titleParams);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 245, 245));
        scrollView.setClipToPadding(false);
        screen.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(8), dp(18), dp(8), dp(16));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView subtitle = new TextView(this);
        subtitle.setText("9 photos from your travels");
        subtitle.setTextColor(Color.rgb(87, 99, 120));
        subtitle.setTextSize(16);
        subtitle.setPadding(dp(26), dp(6), dp(26), dp(22));
        content.addView(subtitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(false);
        content.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int[] images = {
                R.drawable.travel_beach_bg,
                R.drawable.travel_balloons_bg,
                R.drawable.add_trip_mountain_cover,
                R.drawable.travel_beach_bg,
                R.drawable.travel_balloons_bg,
                R.drawable.add_trip_mountain_cover,
                R.drawable.travel_balloons_bg,
                R.drawable.travel_beach_bg,
                R.drawable.add_trip_mountain_cover,
                R.drawable.travel_beach_bg,
                R.drawable.travel_balloons_bg,
                R.drawable.add_trip_mountain_cover,
                R.drawable.travel_beach_bg
        };
        String[] places = {
                "Maldives Beach",
                "Grand Place",
                "Swiss Alps",
                "Tropical Paradise",
                "Bruges Canal",
                "Mountain Valley",
                "Tokyo Tower",
                "Island Lagoon",
                "Alpine Trail",
                "Blue Coast",
                "Old City Walk",
                "Green Hills",
                "Sunny Bay"
        };
        String[] dates = {
                "Jun 15, 2024",
                "Jun 15, 2024",
                "Jun 16, 2024",
                "Jun 16, 2024",
                "Jun 17, 2024",
                "Jun 18, 2024",
                "Jun 20, 2024",
                "Jun 21, 2024",
                "Jun 22, 2024",
                "Jun 23, 2024",
                "Jun 24, 2024",
                "Jun 25, 2024",
                "Jun 26, 2024"
        };

        int initialPhotoCount = 9;
        for (int index = 0; index < initialPhotoCount; index++) {
            grid.addView(createMemoryTile(images[index], places[index], dates[index]));
        }

        TextView loadMoreButton = new TextView(this);
        loadMoreButton.setText("Load More Photos");
        loadMoreButton.setTextColor(Color.WHITE);
        loadMoreButton.setTextSize(16);
        loadMoreButton.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        loadMoreButton.setGravity(Gravity.CENTER);
        loadMoreButton.setClickable(true);
        loadMoreButton.setFocusable(true);
        GradientDrawable loadMoreBackground = new GradientDrawable();
        loadMoreBackground.setColor(NAV_ACTIVE_COLOR);
        loadMoreBackground.setCornerRadius(dp(28));
        loadMoreButton.setBackground(loadMoreBackground);
        loadMoreButton.setElevation(dp(3));
        LinearLayout.LayoutParams loadMoreParams = new LinearLayout.LayoutParams(dp(226), dp(52));
        loadMoreParams.gravity = Gravity.CENTER_HORIZONTAL;
        loadMoreParams.setMargins(0, dp(38), 0, dp(30));
        content.addView(loadMoreButton, loadMoreParams);
        loadMoreButton.setOnClickListener(view -> {
            int currentCount = grid.getChildCount();
            int nextCount = Math.min(images.length, currentCount + 4);
            for (int index = currentCount; index < nextCount; index++) {
                grid.addView(createMemoryTile(images[index], places[index], dates[index]));
            }
            subtitle.setText(nextCount + " photos from your travels");
            if (nextCount >= images.length) {
                loadMoreButton.setVisibility(View.GONE);
            }
        });

        return screen;
    }

    private View createMemoryTile(int imageResId, String place, String date) {
        FrameLayout tile = new FrameLayout(this);
        tile.setClickable(true);
        tile.setFocusable(true);

        ImageView image = new ImageView(this);
        image.setImageResource(imageResId);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setClickable(false);
        tile.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View shade = new View(this);
        shade.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.TRANSPARENT, Color.argb(185, 0, 0, 0)}
        ));
        shade.setVisibility(View.GONE);
        shade.setClickable(false);
        tile.addView(shade, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(16), 0, dp(12), dp(18));
        info.setVisibility(View.GONE);
        info.setClickable(false);

        TextView placeText = new TextView(this);
        placeText.setText(place);
        placeText.setTextColor(Color.WHITE);
        placeText.setTextSize(16);
        placeText.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        info.addView(placeText);

        TextView dateText = new TextView(this);
        dateText.setText(date);
        dateText.setTextColor(Color.WHITE);
        dateText.setTextSize(14);
        dateText.setPadding(0, dp(6), 0, 0);
        info.addView(dateText);

        FrameLayout.LayoutParams infoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        tile.addView(info, infoParams);

        attachMemoryReveal(tile, shade, info);
        attachMemoryReveal(image, shade, info);
        attachMemoryReveal(shade, shade, info);
        attachMemoryReveal(info, shade, info);
        attachMemoryReveal(placeText, shade, info);
        attachMemoryReveal(dateText, shade, info);
        tile.setOnClickListener(view -> showMemoryInfo(shade, info));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(200);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        tile.setLayoutParams(params);
        return tile;
    }

    private void attachMemoryReveal(View target, View shade, View info) {
        View.OnHoverListener memoryHoverListener = (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                    || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                cancelPendingMemoryHide();
                showMemoryInfo(shade, info);
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT
                    && activeMemoryShade == shade
                    && !isPointerInsideView(view, event)) {
                scheduleMemoryHide(shade);
            }
            return true;
        };
        target.setOnHoverListener(memoryHoverListener);
        target.setOnGenericMotionListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                    || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                cancelPendingMemoryHide();
                showMemoryInfo(shade, info);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT && activeMemoryShade == shade) {
                scheduleMemoryHide(shade);
                return true;
            }
            return false;
        });
        target.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                cancelPendingMemoryHide();
                showMemoryInfo(shade, info);
                return false;
            }
            if ((event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL)
                    && activeMemoryShade == shade
                    && !isPointerInsideView(view, event)) {
                scheduleMemoryHide(shade);
            }
            return false;
        });
    }

    private void showMemoryInfo(View shade, View info) {
        cancelPendingMemoryHide();
        if (activeMemoryShade != shade) {
            hideActiveMemoryInfo();
        }
        shade.setVisibility(View.VISIBLE);
        info.setVisibility(View.VISIBLE);
        activeMemoryShade = shade;
        activeMemoryInfo = info;
    }

    private void hideActiveMemoryInfo() {
        cancelPendingMemoryHide();
        if (activeMemoryShade != null) {
            activeMemoryShade.setVisibility(View.GONE);
        }
        if (activeMemoryInfo != null) {
            activeMemoryInfo.setVisibility(View.GONE);
        }
        activeMemoryShade = null;
        activeMemoryInfo = null;
    }

    private void scheduleMemoryHide(View shade) {
        cancelPendingMemoryHide();
        pendingMemoryHide = () -> {
            if (activeMemoryShade == shade) {
                hideActiveMemoryInfo();
            }
        };
        if (rootContainer != null) {
            rootContainer.postDelayed(pendingMemoryHide, 80);
        }
    }

    private void cancelPendingMemoryHide() {
        if (pendingMemoryHide != null && rootContainer != null) {
            rootContainer.removeCallbacks(pendingMemoryHide);
        }
        pendingMemoryHide = null;
    }

    private boolean isPointerInsideView(View view, MotionEvent event) {
        return event.getX() >= 0
                && event.getX() <= view.getWidth()
                && event.getY() >= 0
                && event.getY() <= view.getHeight();
    }

    private void showHomeContent() {
        if (notificationsContent != null) {
            notificationsContent.setVisibility(View.GONE);
        }
        if (memoriesContent != null) {
            memoriesContent.setVisibility(View.GONE);
        }
        hideActiveMemoryInfo();
        addTripButton.setVisibility(View.VISIBLE);
        addTripButton.bringToFront();
        navigationBar.bringToFront();
    }

    private void selectNavigationItem(int selectedIndex) {
        for (int index = 0; index < navigationIcons.length; index++) {
            int color = index == selectedIndex ? NAV_ACTIVE_COLOR : NAV_INACTIVE_COLOR;
            navigationIcons[index].setImageTintList(ColorStateList.valueOf(color));
            navigationLabels[index].setTextColor(color);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationIcons != null
                && (notificationsContent == null
                || notificationsContent.getVisibility() != View.VISIBLE)
                && (memoriesContent == null
                || memoriesContent.getVisibility() != View.VISIBLE)) {
            selectNavigationItem(0);
        }
        if (tripAdapter != null && tripDao != null) {
            List<Trip> trips = tripDao.getTripsForHome(getConnectedUserId());
            allTrips.clear();
            allTrips.addAll(trips.isEmpty() ? createTrips() : trips);
            filterTrips(searchInput == null ? "" : searchInput.getText().toString());
        }
    }

    private void updateUserHeader() {
        TextView greetingText = findViewById(R.id.txt_user_greeting);
        TextView initialsText = findViewById(R.id.txt_user_initials);

        String name = getConnectedUserName();
        greetingText.setText("Bonjour, " + name);
        initialsText.setText(getInitials(name));
    }

    private String getConnectedUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return "Voyageur";
        }

        String displayName = user.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            return displayName.trim();
        }

        String email = user.getEmail();
        if (!TextUtils.isEmpty(email) && email.contains("@")) {
            return email.substring(0, email.indexOf("@")).trim();
        }

        return "Voyageur";
    }

    private String getConnectedUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "guest" : user.getUid();
    }

    private String getInitials(String name) {
        if (TextUtils.isEmpty(name)) {
            return "T";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }

        return initials.length() == 0 ? "T" : initials.toString();
    }

    private List<Trip> createTrips() {
        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip("A VENIR", "Paradis aux Maldives", "15 juin - 22 juin 2024", "5 lieux", R.drawable.travel_beach_bg));
        trips.add(new Trip(null, "Paris et Bruxelles", "5 aout - 18 aout 2024", "8 lieux", R.drawable.travel_balloons_bg));
        trips.add(new Trip(null, "Aventure dans les Alpes suisses", "10 septembre - 20 septembre 2024", "4 lieux", R.drawable.travel_beach_bg));
        trips.add(new Trip("VOYAGES PASSES", "Escapade a Venise", "12 mars - 19 mars 2024", "3 lieux", R.drawable.travel_balloons_bg));
        return trips;
    }

    private void filterTrips(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        List<Trip> filtered = new ArrayList<>();
        for (Trip trip : allTrips) {
            if (TextUtils.isEmpty(normalizedQuery) || matchesTrip(trip, normalizedQuery)) {
                filtered.add(trip);
            }
        }
        applyFrenchSections(filtered);
        tripAdapter.setTrips(filtered);
        noTripsText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesTrip(Trip trip, String query) {
        return contains(trip.getName(), query)
                || contains(trip.getDestination(), query)
                || contains(trip.getDates(), query)
                || contains(trip.getLocations(), query)
                || contains(trip.getHotelName(), query)
                || contains(trip.getHotelAddress(), query)
                || contains(trip.getNotes(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void applyFrenchSections(List<Trip> trips) {
        boolean hasUpcomingSection = false;
        boolean hasPastSection = false;
        for (Trip trip : trips) {
            boolean past = Trip.TYPE_PAST.equals(trip.getTripType())
                    || "VOYAGES PASSES".equals(trip.getSection())
                    || "PAST TRIPS".equals(trip.getSection());
            if (past) {
                trip.setSection(hasPastSection ? null : "VOYAGES PASSES");
                hasPastSection = true;
            } else {
                trip.setSection(hasUpcomingSection ? null : "A VENIR");
                hasUpcomingSection = true;
            }
        }
    }
}
