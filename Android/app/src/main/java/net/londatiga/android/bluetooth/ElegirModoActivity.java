package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ElegirModoActivity  extends Activity {

    private Button btnElegirManual, btnElegirAutomatico;
    private String address;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seleccion_modo);



        //traer la mac del intent device list
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        address= extras.getString("Direccion_Bluethoot");


        btnElegirManual =(Button) findViewById(R.id.btnElegirManual);
        btnElegirAutomatico = (Button) findViewById(R.id.btnElegirAutomatico);
        btnElegirAutomatico.setOnClickListener(btnButtonElegirAutomaticoListener);
        btnElegirManual.setOnClickListener(btnButtonElegirManualListener);

    }

    private View.OnClickListener btnButtonElegirAutomaticoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(ElegirModoActivity.this, AutomaticoModoActivity.class);
            i.putExtra("Direccion_Bluethoot", address);
            startActivity(i);

        }};

    private View.OnClickListener btnButtonElegirManualListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent i = new Intent(ElegirModoActivity.this, activity_comunicacion.class);
            i.putExtra("Direccion_Bluethoot", address);
            startActivity(i);
        }};



}
