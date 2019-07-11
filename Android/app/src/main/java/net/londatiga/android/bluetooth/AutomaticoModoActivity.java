package net.londatiga.android.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.utils.RestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class AutomaticoModoActivity extends Activity implements AdapterView.OnItemSelectedListener {

    TextView tvMensaje, tvApi,tvApi2, tvApi3;
    Spinner spinner_Planta ;
    Button btnButton;
    public static final String BROADCAST_ACTION = "net.londatiga.android.bluetooth";
    String baseUrl = "http://restapisoa.dx.am/restapi/v1";
    private final String TAG = "AutomaticoModoActivity";
    private Integer indiceLang ;
    private String localidadObtenida;
    protected LocationManager locationManager;
    protected  Localizacion localizacion;
    private  Integer probabilidad,huMax,tempMax,huMin,tempMin;
    private String tipoPlanta;
    private String tipoSuelo;

    private ArrayList<BluetoothDevice> mDeviceList;
    private String modoElegido,address;

    HiloApi hiloApi;
    public void setLocalidadObtenida(String parametro){
        this.localidadObtenida = parametro;
        hiloApi.start();
        locationManager.removeUpdates(localizacion);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        address= extras.getString("Direccion_Bluethoot");


        hiloApi = new HiloApi();
        setContentView(R.layout.automatico_modo);

        tvMensaje = findViewById(R.id.tvMensaje);
        tvApi = findViewById(R.id.tvApi);
        tvApi2 = findViewById(R.id.tvApi2);
        tvApi3 = findViewById(R.id.tvApi3);

        //Se llena el spinner con los tipos de planta
        spinner_Planta = findViewById(R.id.sp_TipoPlanta);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.plantas,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Planta.setAdapter(adapter);
        spinner_Planta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //@Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                tipoPlanta= (String) adapterView.getItemAtPosition(position);
            }

            //@Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                tipoPlanta= (String) parent.getItemAtPosition(0);
            }
        });


        btnButton =  (Button) findViewById(R.id.button);
        btnButton.setOnClickListener(btnButtonListener);



    }

    public boolean checkLocationPermission(String permission)
    {
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }



    public void onResume() {
        super.onResume();

        if(!checkLocationPermission("android.permission.ACCESS_COARSE_LOCATION")){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, 1020);
        } else {

            iniciarLocalizacion();
        }
    }

    public void showSettingsAlert(String provider) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AutomaticoModoActivity.this);

        alertDialog.setTitle(provider + " SETTINGS");

        alertDialog
                .setMessage(provider + " no esta activado. Desea activarlo?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(settingsIntent);

                    }
                });
    }

    private void iniciarLocalizacion(){


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         localizacion = new Localizacion();

        localizacion.setAutomaticoModoActivity(this, tvMensaje);

        final boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnable) {
            showSettingsAlert("GPS");
        }
        if(!checkLocationPermission("android.permission.ACCESS_FINE_LOCATION")){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1001);

        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,localizacion,null);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,localizacion,null);
        tvMensaje.setText("Obteniendo ubicacion ...");


    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarLocalizacion();
                return;
            }

        }
    }

    public void showPlantaInexisisteAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AutomaticoModoActivity.this);

        alertDialog.setTitle("Planta inexistente");

        alertDialog
                .setMessage("Vuelva a elegir el tipo de planta");

        alertDialog.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                });

        alertDialog.show();
    }

    private class HiloApi extends Thread {
        public void run()
        {
            setEstadistica(localidadObtenida);
            setClima(localidadObtenida);
            setTipoSuelo();
        }

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void setTipoSuelo()
    {
        if(indiceLang<33)
        {
            tipoSuelo="Seco";
        }
        else
        {
            if(indiceLang<67)
            {
                tipoSuelo="Calido";
            }
            else
            {
                tipoSuelo="Humedo";
            }

        }

    }


    public void setRangoTemperaturas() throws JSONException, IOException,MalformedURLException {

        String urlApiClima = this.baseUrl + "/"+tipoSuelo+ "/"+tipoPlanta;
        RestUtils restApiClima = new RestUtils(urlApiClima);
        try {
            JSONObject jsonObject = restApiClima.consumirApi();
            this.huMax = jsonObject.getJSONObject("suelo").getInt("humax");
            this.tempMax = jsonObject.getJSONObject("suelo").getInt("tempmax");
            this.huMin = jsonObject.getJSONObject("suelo").getInt("humin");
            this.tempMin = jsonObject.getJSONObject("suelo").getInt("tempmin");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e1) {
            e1.printStackTrace();
            throw e1;

        } catch (JSONException e2) {
            e2.printStackTrace();
            throw e2;
        }
    }

    public void setEstadistica(String localidadRecibida) {
        String urlApiClima = this.baseUrl + "/" + localidadRecibida + "/estadistica";
        RestUtils restApiClima = new RestUtils(urlApiClima);
        try {
            JSONObject jsonObject = restApiClima.consumirApi();
            Integer precipitacion = jsonObject.getJSONObject("estadistica").getInt("precipitacionAnual");
            Integer temperaturaAnual = jsonObject.getJSONObject("estadistica").getInt("temperaturaAnual");

            indiceLang = precipitacion / temperaturaAnual ;
           // tvApi3.setText("indiceLang: "+indiceLang) ;
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e1) {
            e1.printStackTrace();

        } catch (JSONException e2) {
            e2.printStackTrace();
        }

    }

    public void setClima(String localidadRecibida) {
        String urlApiClima = this.baseUrl + "/" + localidadRecibida + "/clima";
        RestUtils restApiClima = new RestUtils(urlApiClima);
        try {
            JSONObject jsonObject = restApiClima.consumirApi();
            String nombre = jsonObject.getJSONObject("clima").getString("nombre");
            Integer temperatura = jsonObject.getJSONObject("clima").getInt("temperatura");
             this.probabilidad = jsonObject.getJSONObject("clima").getInt("probabilidad");


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e1) {
            e1.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener btnButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            try {
                setRangoTemperaturas();
                Intent intent = new Intent(AutomaticoModoActivity.this, AutomaticoTransferenciaArduino.class);
                intent.putExtra("Direccion_Bluethoot",address);
                intent.putExtra("tipoPlantaRecibida",tipoPlanta);
                intent.putExtra("humedadRecibida",Integer.toString(huMax));
                intent.putExtra("sueloRecibido",tipoSuelo);
                intent.putExtra("temperaturaRecibida",Integer.toString(tempMax));

                startActivity(intent);
            } catch (JSONException e) {
                showPlantaInexisisteAlert();
            } catch (IOException e) {
                showPlantaInexisisteAlert();
            }

        }

    };

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}



