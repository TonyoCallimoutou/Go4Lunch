package com.tonyocallimoutou.go4lunch.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tonyocallimoutou.go4lunch.R;
import com.tonyocallimoutou.go4lunch.model.User;
import com.tonyocallimoutou.go4lunch.model.places.RestaurantDetails;
import com.tonyocallimoutou.go4lunch.ui.autocomplete.AutocompleteFragment;
import com.tonyocallimoutou.go4lunch.ui.detail.DetailsActivity;
import com.tonyocallimoutou.go4lunch.ui.listview.ListViewFragment;
import com.tonyocallimoutou.go4lunch.ui.mapview.MapViewFragment;
import com.tonyocallimoutou.go4lunch.ui.setting.SettingActivity;
import com.tonyocallimoutou.go4lunch.ui.workmates.WorkmatesFragment;
import com.tonyocallimoutou.go4lunch.utils.UtilNotification;
import com.tonyocallimoutou.go4lunch.viewmodel.ViewModelChat;
import com.tonyocallimoutou.go4lunch.viewmodel.ViewModelFactory;
import com.tonyocallimoutou.go4lunch.viewmodel.ViewModelRestaurant;
import com.tonyocallimoutou.go4lunch.viewmodel.ViewModelUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.bottom_nav_view)
    BottomNavigationView navigationView;

    private ViewModelUser viewModelUser;
    private ViewModelRestaurant viewModelRestaurant;
    private ViewModelChat viewModelChat;
    private View sideView;
    private ActionBar actionBar;

    private User currentUser;

    private List<RestaurantDetails> nearbyRestaurant = new ArrayList<>();
    private List<RestaurantDetails> bookedRestaurant = new ArrayList<>();
    private List<User> workmates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check Google play service
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(status == ConnectionResult.SUCCESS) {
            viewModelUser = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(ViewModelUser.class);
            viewModelRestaurant = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(ViewModelRestaurant.class);
            viewModelChat = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(ViewModelChat.class);

            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);

            initActionBar();
            initBottomNavigationView();
        }
        else {
            errorGooglePlayService(status);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(status == ConnectionResult.SUCCESS) {
            if (viewModelUser.isCurrentLogged()) {
                viewModelUser.createUser();
                initData();
            } else {
                startSignInActivity();
            }
        }
    }

    // error Google PLay Service

    private void errorGooglePlayService(int status) {
        String message = "";
        if(status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            message =  getString(R.string.message_alertDialog_google_play_service_update);

        }
        else {
            message =  getString(R.string.message_alertDialog_google_play_service_download);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_alertDialog_google_play_service);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(getResources().getString(R.string.positive_button_alertDialog_permission), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    // SIGN IN ACTIVITY

    public void startSignInActivity() {
        List<AuthUI.IdpConfig> provider = Arrays.asList(
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivity(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(provider)
                        .setIsSmartLockEnabled(false,true)
                        .setLogo(R.drawable.logo)
                        .build()
        );
    }


    // INIT ACTION BAR

    private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawer.isOpen()) {
                    mDrawer.close();
                } else {
                    mDrawer.open();
                }
                return true;
            case R.id.search_menu:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // INIT BOTTOM NAVIGATION

    private void initBottomNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.bottom_nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_list, R.id.navigation_workmates)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    //INIT SIDE VIEW

    private void initSideView() {

        NavigationView nav = findViewById(R.id.side_menu_nav_view);
        nav.setNavigationItemSelectedListener(this);
        sideView = nav.getHeaderView(0);

        if (currentUser.getUrlPicture() != null) {
            setProfilePicture(currentUser.getUrlPicture());
        }
        setTextUser(currentUser);
    }

    private void setProfilePicture(String profilePictureUrl) {
        ImageView profilePicture = sideView.findViewById(R.id.profile_picture_header_side_view);

        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(profilePicture);
    }

    private void setTextUser(User user) {
        TextView email = sideView.findViewById(R.id.user_email);
        TextView name = sideView.findViewById(R.id.user_name);

        email.setText(user.getEmail());
        name.setText(user.getUsername());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigation_your_lunch:
                yourLunch();
                break;
            case R.id.navigation_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_logout:
                mDrawer.close();
                viewModelUser.signOut(this);
                navigationView.setSelectedItemId(R.id.navigation_map);

                startSignInActivity();

                break;
            default:
                break;
        }
        return true;
    }

    // Your Lunch
    public void yourLunch() {
        RestaurantDetails restaurant = viewModelRestaurant.getRestaurantOfCurrentUser();
        if (restaurant != null) {
            DetailsActivity.navigate(this,restaurant);
        }
        else {
            Snackbar.make(mDrawer, getString(R.string.your_lunch), Snackbar.LENGTH_LONG).show();
        }
    }

    // InitData

    public void initData() {

        viewModelUser.setCurrentUserLiveData();
        viewModelUser.setWorkmatesList();
        viewModelRestaurant.setBookedRestaurantList();
        if (nearbyRestaurant.size() == 0) {
            viewModelRestaurant.setNearbyPlace(null);
        }
        viewModelRestaurant.setSearchRestaurant(null);

        viewModelRestaurant.getBookedRestaurantLiveData().observe(this, restaurantsResults -> {
            bookedRestaurant.clear();
            bookedRestaurant.addAll(restaurantsResults);
            UtilNotification.newInstance(null,restaurantsResults,null,null);
            ListViewFragment.setBookedRestaurant(restaurantsResults);
            MapViewFragment.setBookedRestaurant(restaurantsResults);
        });

        viewModelUser.getWorkmates().observe(this, workmates -> {
            UtilNotification.newInstance(workmates,null,null,null);
            WorkmatesFragment.setWorkmates(workmates);
            ListViewFragment.setWorkmates(workmates);
        });

        viewModelRestaurant.getNearbyRestaurantLiveData().observe(this, restaurantsResults -> {
            for (RestaurantDetails restaurant : restaurantsResults) {
                restaurant.getWorkmatesId().clear();
            }
            nearbyRestaurant.clear();
            nearbyRestaurant.addAll(restaurantsResults);
            ListViewFragment.setNearbyRestaurant(restaurantsResults);
            MapViewFragment.setNearbyRestaurant(restaurantsResults);
        });

        viewModelUser.getCurrentUserLiveData().observe(this, currentUserResults -> {
            if (currentUserResults != null) {
                UtilNotification.newInstance(null,null,this,currentUserResults);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences
                        .edit()
                        .putString(getString(R.string.shared_preference_username), currentUserResults.getUsername())
                        .apply();

                currentUser = currentUserResults;
                WorkmatesFragment.setCurrentUser(currentUserResults);
                viewModelChat.setPinsNoReadingMessage(currentUser);
                initSideView();
            }
        });

        viewModelRestaurant.getPredictionLiveData().observe(this, predictionsResults -> {
            AutocompleteFragment.setPredictions(predictionsResults);
        });

        viewModelChat.getNumberNoReadingMessageMap().observe(this, numberNoReading -> {
            WorkmatesFragment.initPins(numberNoReading);
            ListViewFragment.initPins(numberNoReading);
        });
    }
}