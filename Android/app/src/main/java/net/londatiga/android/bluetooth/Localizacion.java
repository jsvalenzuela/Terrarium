package net.londatiga.android.bluetooth;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Localizacion implements LocationListener {
    AutomaticoModoActivity automaticoModoActivity;
    TextView tvMensaje;


    public void setAutomaticoModoActivity(AutomaticoModoActivity automaticoModoActivity, TextView tvMensaje) {
        this.automaticoModoActivity = automaticoModoActivity;
        this.tvMensaje = tvMensaje;
    }


    @Override
    public void onLocationChanged(Location location) {
        //este metodo se ejeta cuando gps recibe coordenandas
        Geocoder geocoder;
        List<Address> direccion = null;
        Geocoder geoCoder = new Geocoder(automaticoModoActivity, Locale.getDefault());
        try {
            direccion = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String localidad = direccion.get(0).getLocality();

        String texto = "Mi ubicacion es: " + localidad;
        tvMensaje.setText(texto);
        localidad = localidad.replace(" " , "%20");
        automaticoModoActivity.setLocalidadObtenida(localidad);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        tvMensaje.setText("GPS activado");
    }

    @Override
    public void onProviderDisabled(String provider) {
        tvMensaje.setText("GPS Desactivado");
    }
}
