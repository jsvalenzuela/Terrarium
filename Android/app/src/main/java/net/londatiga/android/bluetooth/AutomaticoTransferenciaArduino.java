package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class AutomaticoTransferenciaArduino extends Activity {
    TextView txtTemperatura;
    TextView txtHumedad;
    TextView txtEstadoRiego, txtHumedadServicio, txtTemperaturaServicio, txtTipoPlanta, txtTipoSueloRecibido;
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private String huMaxRecibida, temperaturaRecibida, sueloRecibido, plantaRecibida;
    private AutomaticoTransferenciaArduino.ConnectedThread mConnectedThread;
    private String respuestaUser;
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;

    protected  void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatico_transferencia_arduino);

        txtTemperatura=(TextView)findViewById(R.id.tv_SensorTemp);
        txtHumedad=(TextView)findViewById(R.id.tv_SensorHumedad);
        txtEstadoRiego = (TextView) findViewById(R.id.tvEstadoRiego);
        txtEstadoRiego.setVisibility(View.INVISIBLE);
        txtHumedadServicio = (TextView) findViewById(R.id.tvHumedadServicio);
        txtTemperaturaServicio = (TextView) findViewById(R.id.tvTemperaturaServicio);
        txtTipoPlanta = (TextView) findViewById(R.id.tvTipoPlantaTrans);
        txtTipoSueloRecibido = (TextView) findViewById(R.id.tvSueloTrans);
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();
    }



    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    //int endOfLineIndex = recDataString.indexOf("\r\n");
                    int endOfLineIndex = recDataString.indexOf(";");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        if(recDataString.charAt(0)=='#') {
                            String dataInPrint = recDataString.substring(1, endOfLineIndex);
                            String [] sensores=dataInPrint.split("/");
                            txtTemperatura.setText(sensores[0]);
                            txtHumedad.setText(sensores[1]);
                            //txtPotenciometro.setText(dataInPrint);

                        }
                        if(respuestaUser != null ){
                            if(respuestaUser.equals("SI")){

                            }

                        }

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }




    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        address = extras.getString("Direccion_Bluethoot");
        //Le agrego la humedad recibida por el servicio
        huMaxRecibida = getIntent().getExtras().getString("humedadRecibida");
        temperaturaRecibida = getIntent().getExtras().getString("temperaturaRecibida");
        sueloRecibido = getIntent().getExtras().getString("sueloRecibido");
        plantaRecibida = getIntent().getExtras().getString("tipoPlantaRecibida");
        BluetoothDevice device = btAdapter.getRemoteDevice(address);


        txtHumedadServicio.setText(huMaxRecibida);
        txtTipoSueloRecibido.setText(sueloRecibido);
        txtTipoPlanta.setText(plantaRecibida);
        txtTemperaturaServicio.setText(temperaturaRecibida);
        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            showToast("La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }

        //Una vez creado el socket, creo el hilo connectthread
        mConnectedThread = new AutomaticoTransferenciaArduino.ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");   //Al emparejar

        showSettingsAlert();

    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AutomaticoTransferenciaArduino.this);

        alertDialog.setTitle("Alerta de Riego");

        alertDialog
                .setMessage("Desea aprovechar el agua de lluvia?");

        alertDialog.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String modo = "Automatico";
                        respuestaUser = "2";
                        mConnectedThread.write("#"+modo+"/"+huMaxRecibida+"/"+ temperaturaRecibida+"/"+respuestaUser+";");
                        //Escribir en bluetooth el mensaje para enviar el clima y humedad de servicios
                        //mostrar en interfaz el combo regando, dentro de hilo del bluetooth
                        txtEstadoRiego.setText("Estamos trabajando en ello");
                        txtEstadoRiego.setVisibility(View.VISIBLE);

                    }
                });

        alertDialog.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String modo = "Automatico";
                        respuestaUser = "1";
                        mConnectedThread.write("#"+modo+"/"+huMaxRecibida+"/"+ temperaturaRecibida+"/"+respuestaUser+";");
                        //Escribir en bluetooth el mensaje para enviar el clima y humedad de servicios
                        //mostrar en interfaz el combo regando, dentro de hilo del bluetooth
                        txtEstadoRiego.setText("Estamos trabajando en ello");
                        txtEstadoRiego.setVisibility(View.VISIBLE);
                    }
                });

        alertDialog.show();
    }



    public void onPause()
    {

          super.onPause();
            try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
        }

        private class ConnectedThread extends Thread
        {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {

            int indexOfSubStr = input.indexOf('x');
            String inputEnviar;
            if(indexOfSubStr != -1
            )
                inputEnviar= input.substring(indexOfSubStr);
            else
                inputEnviar = input;
            byte[] msgBuffer = inputEnviar.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                finish();
            }
        }
    }


}
