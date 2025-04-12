package com.lochana.parkingassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lochana.parkingassistant.ui.home.HomeFragment;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomParkingInfoWindow extends InfoWindow {

    private String availability;
    private String price;
    private View mView;
    private BottomSheetDialog bottomSheetDialog;
    private Button navigateButton;
    private HomeFragment homeFragmentInstance;
    public CustomParkingInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        Context context = mapView.getContext();
        mView = LayoutInflater.from(context).inflate(layoutResId, null);
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(mView);

        navigateButton = mView.findViewById(R.id.button4);
    }

    public void setParkingDetails(String availability, String price) {
        this.availability = availability;
        this.price = price;
    }

    @Override
    public void onOpen(Object item) {
        Marker marker = (Marker) item;
        TextView title = mView.findViewById(R.id.info_window_title);
        TextView availabilityView = mView.findViewById(R.id.info_window_availability);
        TextView priceView = mView.findViewById(R.id.info_window_price);
        LinearLayout infoWindowLayout = mView.findViewById(R.id.info_window_layout); // Get reference to the layout

        title.setText(marker.getTitle());
        availabilityView.setText(availability);
        priceView.setText(price);

        // You can access and set more views from your custom layout here
        // Set OnClickListener to close the InfoWindow when the layout is clicked
        infoWindowLayout.setOnClickListener(v -> {
            close(); // Call the close() method of the InfoWindow
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onClose() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    @Override
    public void close() {
        super.close();
        onClose(); // Ensure our custom onClose logic is executed
    }

    public static void closeAllInfoWindowsOn(MapView mapView) {
        for (int i = 0; i < mapView.getOverlays().size(); i++) {
            if (mapView.getOverlays().get(i) instanceof Marker) {
                ((Marker) mapView.getOverlays().get(i)).closeInfoWindow();
            }
        }
    }




}