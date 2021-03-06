package com.tonyocallimoutou.go4lunch.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tonyocallimoutou.go4lunch.model.User;
import com.tonyocallimoutou.go4lunch.model.places.RestaurantDetails;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final String COLLECTION_NAME = "users";

    private User currentUser;

    private static volatile UserRepository instance;

    private UserRepository() {}

    // Instance

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    // My Firestore Collection

    public CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // Get Current User

    public boolean isCurrentLogged() {
        return this.getCurrentFirebaseUser() != null;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public User getCurrentUser() {

        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null) {
            if (currentUser == null || ! currentUser.getUid().equals(user.getUid())) {

                return null;
            }
            return currentUser;
        }
        return null;
    }

    public void createUser() {

        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null) {

            getUsersCollection().get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            boolean isAlreadyExisting = false;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot document : list) {
                                    User workmate = document.toObject(User.class);
                                    if (workmate.getUid().equals(user.getUid())) {
                                        isAlreadyExisting = true;
                                        currentUser = workmate;
                                    }
                                }
                            }
                            if ( ! isAlreadyExisting) {

                                String urlPicture = (user.getPhotoUrl() != null) ?
                                        user.getPhotoUrl().toString()
                                        : null;
                                String username = user.getDisplayName();
                                String uid = user.getUid();
                                String email = user.getEmail();

                                currentUser = new User(uid, username, urlPicture,email);

                                getUsersCollection().document(currentUser.getUid()).set(currentUser);
                            }
                        }
                    });
        }
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        getUsersCollection().document(currentUser.getUid()).delete();
        currentUser = null;
        return signOut(context);
    }

    public void setNameOfCurrentUser(String name) {
        currentUser.setUsername(name);
        getUsersCollection().document(currentUser.getUid()).set(currentUser);
    }

    // List of Workmates

    public void setWorkmatesList(MutableLiveData<List<User>> liveData) {

        getUsersCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w("TAG", "Listen failed.", error);
                    return;
                }

                List<User> workmatesList = new ArrayList<>();
                for (DocumentSnapshot document : value) {
                    User user = document.toObject(User.class);
                    workmatesList.add(user);
                }

                liveData.setValue(workmatesList);
            }
        });
    }



    // Booked Restaurant

    public void bookedRestaurant(RestaurantDetails restaurant) {
        currentUser.setBookedRestaurant(restaurant);
        getUsersCollection().document(currentUser.getUid()).set(currentUser);
    }

    public void cancelRestaurant() {
        currentUser.setBookedRestaurant(null);
        getUsersCollection().document(currentUser.getUid()).set(currentUser);
    }

    // Like restaurant

    public void likeThisRestaurant(RestaurantDetails restaurant){
        List<String> listRestaurantId = getCurrentUser().getLikeRestaurantId();
        if ( ! listRestaurantId.contains(restaurant.getPlaceId())) {
            listRestaurantId.add(restaurant.getPlaceId());
        }

        getCurrentUser().setLikeRestaurantId(listRestaurantId);
        getUsersCollection().document(currentUser.getUid()).set(currentUser);
    }

    public void dislikeThisRestaurant(RestaurantDetails restaurant) {
        List<String> listRestaurantId = getCurrentUser().getLikeRestaurantId();
        listRestaurantId.remove(restaurant.getPlaceId());

        getCurrentUser().setLikeRestaurantId(listRestaurantId);
        getUsersCollection().document(currentUser.getUid()).set(currentUser);
    }


    public void setCurrentUserLivedata(MutableLiveData<User> liveData) {
        getUsersCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w("TAG", "Listen failed.", error);
                    return;
                }

                for (DocumentSnapshot document : value) {
                    User user = document.toObject(User.class);
                    if (getCurrentFirebaseUser()!=null) {
                        if (user.getUid().equals(getCurrentFirebaseUser().getUid())) {
                            liveData.setValue(user);
                        }
                    }

                }
            }
        });
    }
}
