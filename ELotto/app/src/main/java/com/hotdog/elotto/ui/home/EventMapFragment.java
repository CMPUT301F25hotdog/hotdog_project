    package com.hotdog.elotto.ui.home;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.LatLngBounds;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.GeoPoint;
    import com.hotdog.elotto.R;
    import com.hotdog.elotto.model.Event;

    import java.util.Map;

    public class EventMapFragment extends Fragment implements OnMapReadyCallback {
        //https://developers.google.com/maps/documentation/android-sdk/map#maps_android_map_fragment-java
        //used for most of the class
        private GoogleMap gMap;
        private String eventId;
        private FloatingActionButton backButtonMap;
        /**
         * Creates the layout for the map screen, retrieves the event ID from arguments,
         * initializes the back button, and prepares the Google Map fragment.
         *
         * @param inflater  the LayoutInflater used to inflate the fragment's layout
         * @param container the parent view group
         * @param savedInstanceState previously saved state, or null if none, used to return
         * @return the view for this fragment
         */
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_event_map, container, false);
            backButtonMap = view.findViewById(R.id.backButtonMap);
            backButtonMap.setOnClickListener(v -> {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
            if (getArguments() != null) {
                eventId = getArguments().getString("eventId");
            }
            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
            return view;
        }
        /**
         * Called when the Google Map is ready, stores the map reference
         * and begins loading entrant locations from Firestore.
         *
         * @param googleMap the GoogleMap instance
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            gMap = googleMap;
            loadEntrantGeoPoints();
        }
        /**
         * Loads entrant location data for the selected event from Firestore, adds them to the map as markers,
         * labels each marker with the corresponding entrants name
         */
        private void loadEntrantGeoPoints() {
            if (eventId == null) {
                Toast.makeText(getContext(), "No event ID provided", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("events")
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(getContext(), "Event Does Not Exist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Event event = doc.toObject(Event.class);
                        if (event == null) {
                            return;
                        }
                        Map<String, GeoPoint> locs = event.getEntrantLocations();
                        if (locs == null || locs.isEmpty()) {
                            Toast.makeText(getContext(), "No Entrant Locations to Display", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                        final int[] completed = {0};
                        final int total = locs.size();
                        for (Map.Entry<String, GeoPoint> entry : locs.entrySet()) {
                            String userId = entry.getKey();
                            GeoPoint gp = entry.getValue();
                            if (gp == null) {
                                continue;
                            }
                            LatLng pos = new LatLng(gp.getLatitude(), gp.getLongitude());
                            db.collection("users").document(userId)
                                            .get()
                                            .addOnSuccessListener(userDoc-> {
                                                String name = "placeholder";
                                                if (userDoc.exists()) {
                                                    name = userDoc.getString("name");
                                                    if (name == null) name = "Unknown";
                                                }
                                                gMap.addMarker(new MarkerOptions().position(pos).title(name));
                                                bounds.include(pos);
                                                completed[0]++;
                                                if (completed[0] == total) {
                                                    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
                                                }
                                            }).addOnFailureListener(e->{
                                            gMap.addMarker(new MarkerOptions().position(pos).title("Unknown"));
                                            bounds.include(pos);
                                            completed[0]++;
                                            if (completed[0] == total) {
                                                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
                                            }
                                            });

                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load map data", Toast.LENGTH_SHORT).show());
        }


    }
