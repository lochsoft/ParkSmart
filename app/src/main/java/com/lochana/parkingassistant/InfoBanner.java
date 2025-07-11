package com.lochana.parkingassistant;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class InfoBanner extends InfoWindow {

    public InfoBanner(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        LinearLayout banner_layout = mView.findViewById(R.id.banner_layout);
        banner_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }});
//        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
//            @Override
//            public boolean singleTapConfirmedHelper(GeoPoint p) {
//                InfoWindow.closeAllInfoWindowsOn(mapView);
//                return true;
//            }
//
//            @Override
//            public boolean longPressHelper(GeoPoint p) {
//                return false;
//            }
//        });

        //mapView.getOverlays().add(eventsOverlay);

    }

    @Override
    public void onOpen(Object item) {
        Marker marker = (Marker) item;

        TextView title = mView.findViewById(R.id.bubble_title);

        title.setText(marker.getTitle());
    }

    @Override
    public void onClose() {
        // Optional: Do something when the info window closes
    }

}
