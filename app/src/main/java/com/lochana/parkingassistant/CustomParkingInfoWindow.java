package com.lochana.parkingassistant;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomParkingInfoWindow extends InfoWindow {

    private String availability;
    private String price;

    public CustomParkingInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
    }

    public void setParkingDetails(String availability, String price) {
        this.availability = availability;
        this.price = price;
    }

    @Override
    public void onOpen(Object item) {
        Marker marker = (Marker) item;
        TextView title = (TextView) mView.findViewById(R.id.info_window_title);
        TextView availabilityView = (TextView) mView.findViewById(R.id.info_window_availability);
        TextView priceView = (TextView) mView.findViewById(R.id.info_window_price);
        LinearLayout infoWindowLayout = (LinearLayout) mView.findViewById(R.id.info_window_layout); // Get reference to the layout


        title.setText(marker.getTitle());
        availabilityView.setText("Availability: " + availability);
        priceView.setText("Price: " + price);

        // You can access and set more views from your custom layout here
        // Set OnClickListener to close the InfoWindow when the layout is clicked
        infoWindowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(); // Call the close() method of the InfoWindow
            }
        });
    }

    @Override
    public void onClose() {
        // Optional: Handle any cleanup when the InfoWindow closes
    }

    public static void closeAllInfoWindowsOn(MapView mapView) {
        for (int i = 0; i < mapView.getOverlays().size(); i++) {
            if (mapView.getOverlays().get(i) instanceof Marker) {
                ((Marker) mapView.getOverlays().get(i)).closeInfoWindow();
            }
        }
    }
}