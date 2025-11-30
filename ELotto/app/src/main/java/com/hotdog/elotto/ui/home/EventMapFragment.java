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

        private GoogleMap gMap;
        private String eventId;
        private FloatingActionButton backButtonMap;
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

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            gMap = googleMap;
            loadEntrantGeoPoints();
        }
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
                            Toast.makeText(getContext(), "NOOOO", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Event event = doc.toObject(Event.class);
                        if (event == null) {
                            return;
                        }
                        Map<String, GeoPoint> locs = event.getEntrantLocations();
                        if (locs == null || locs.isEmpty()) {
                            Toast.makeText(getContext(), "PEALPELASPLEASE", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

                        for (Map.Entry<String, GeoPoint> entry : locs.entrySet()) {
                            GeoPoint gp = entry.getValue();
                            if (gp == null) {
                                continue;
                            }
                            LatLng pos = new LatLng(gp.getLatitude(), gp.getLongitude());
                            gMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title("Entrant ;-;"));
                            bounds.include(pos);
                        }
                        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load map data", Toast.LENGTH_SHORT).show());
        }


    }
