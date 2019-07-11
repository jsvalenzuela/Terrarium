package net.londatiga.android.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//Agregado
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.lang.Math;

/*import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class activity_comunicacion extends Activity
{


    //Agregado
    TextView txtTemperatura;
    TextView txtHumedad;
    Spinner comboModo;
    Switch switchVentilacion;
    Switch swicthTecho;
    Switch swicthIluminacion;
    Switch switchRiego;

    private String modo;
    private String ventilacion;
    private String techoDescubierto;
    private String iluminacion;
    private String riego;

    //Se utilizar para detectar el shake
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //se inicializan variables
        modo="Manual";
        ventilacion="NO";
        techoDescubierto="NO";
        iluminacion="NO";
        riego = "NO";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);

        //Se definen los componentes del layout

        //agregados
        txtTemperatura=(TextView)findViewById(R.id.tv_SensorTemp);
        txtHumedad=(TextView)findViewById(R.id.tv_SensorHumedad);
        comboModo = (Spinner) findViewById(R.id.sp_Modo);
        switchVentilacion=(Switch) findViewById(R.id.sw_Ventilacion);
        swicthTecho=(Switch) findViewById(R.id.sw_TechoDescubierto);
        swicthIluminacion=(Switch) findViewById(R.id.sw_Iluminacion);
        switchRiego = (Switch)findViewById(R.id.sw_Riego);


        //Configuracion del switch de ventilacion
        switchVentilacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ventilacion="SI";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Ventilacion Activada");
                } else {
                    ventilacion="NO";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Ventilacion Desactivada");
                }
            }
        });

        //Configuracion del switch de techo
        swicthTecho.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    techoDescubierto="SI";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Techo Descubierto Activado");
                } else {
                    techoDescubierto="NO";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Techo Descubierto Desactivado");
                }
            }
        });

        //Configuracion del switch de iluminacion
        swicthIluminacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    iluminacion="SI";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Iluminacion Activada");
                } else {
                    iluminacion="NO";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+";");
                    showToast("Iluminacion Desactivada");
                }
            }
        });

        //Configuracion del switch de riego
        switchRiego.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    riego="SI";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+"/"+riego+";");
                    showToast("Riego Activado");
                } else {
                    riego="NO";
                    mConnectedThread.write("#"+modo+"/"+ventilacion+"/"+techoDescubierto+"/"+iluminacion+"/"+riego+";");
                    showToast("Riego Desactivado");
                }
            }
        });

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();


        // ShakeDetector inicializacion
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */

                //corre el switch de iluminacion
                if(swicthIluminacion.isChecked())
                    swicthIluminacion.setChecked(false);
                else
                    swicthIluminacion.setChecked(true);
            }
        });

    }


    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        address= extras.getString("Direccion_Bluethoot");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");

        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);

        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
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

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    //Listener del boton encender que envia  msj para enceder Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnEncenderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("1");    // Send "1" via Bluetooth
            showToast("Encender el LED");        }
    };

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnApagarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("2");    // Send "0" via Bluetooth
            showToast("Apagar el LED");
        }
    };

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //******************************************** Hilo secundario del Activity**************************************
    //*************************************** recibe los datos enviados por el HC05**********************************

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
