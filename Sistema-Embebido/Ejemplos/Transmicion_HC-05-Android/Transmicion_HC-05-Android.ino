#include <Servo.h>
#include <SoftwareSerial.h>
#include "DHT.h"
//Al utilizar la biblioteca SoftwareSerial los pines RX y TX para la transmicion serie de Bluethoot se pueden cambiar mapear a otros pines.
//Sino se utiliza esta bibioteca esto no se puede realizar y se debera conectar al pin 0 y 1, conexion Serie no pudiendo imprmir por el monitor serie
//Al estar estos ocupados.
SoftwareSerial BTserial(10,11); // RX | TX
// Uncomment whatever type you're using!
#define DHTTYPE DHT11   // DHT 11
#define DHTPIN 7

char c = ' ';
String mensaje="";
String modo="Manual";
String ventilacion="NO";
String techoDescubierto="NO";
String iluminacion="NO";
String riego="NO";

int pinLed=13;

// **** PINES DIGITALES  ****
//const int PIN_TEMPERATURA =7 ;
const int PIN_RELE_COOLER = 4 ;
const int PIN_RELE_BOMBA = 8 ;
const int PIN_SERVO_IZQ = 2;//digital
const int PIN_SERVO_DER = 3;//digital

// **** PINES ANALOGICOS ****
const int PIN_HUMEDAD = A0;

DHT dht(DHTPIN, DHTTYPE);


Servo servoIzq;
Servo servoDer;
unsigned long tiempo_ultima_medicion;
void setup()
{
    //Se configura la velocidad del puerto serie para poder imprimir en el puerto Serie
    Serial.begin(9600);
    Serial.println("Inicializando Terrarium...");
  
    //Se configura la velocidad de transferencia de datos entre el Bluethoot  HC05 y el de Android.
    BTserial.begin(9600); 
    Serial.println("Modo: "+modo);
    Serial.println("Ventilacion: "+ventilacion);
    Serial.println("Techo descubierto: "+techoDescubierto);
    Serial.println("Iluminacion: "+iluminacion);
    Serial.println("Riego: "+riego);
    Serial.println("");

    pinMode(pinLed, OUTPUT);
    //dht.begin();
    servoIzq.attach(PIN_SERVO_IZQ);
    servoDer.attach(PIN_SERVO_DER);
    tiempo_ultima_medicion = millis();
}
 
void loop()
{
   //sI reciben datos del HC05 
    if (BTserial.available())
    { 
        //se los lee y se los muestra en el monitor serie
         mensaje= BTserial.readString();
         Serial.println(mensaje);
         analizarMensaje(mensaje);
        //analizarDato(c);
    }

    //Si se ingresa datos por teclado en el monitor serie 
    //if (Serial.available())
    //{
        //se los lee y se los envia al HC05
        //c =  Serial.read();
        //Serial.write(c);
        //BTserial.print(c); 
    //}

    // Read temperature as Celsius (the default)
    //float t = dht.readTemperature();

    // Check if any reads failed and exit early (to try again).
  //if (isnan(t)) {
    //BTserial.print("#Failed to read from DHT sensor!ç");
    //return;
  //}


    int humedad = analogRead(PIN_HUMEDAD);
    int humedadPorcentaje = map(humedad, 1023, 0, 0, 100);
    float temperature = dht.readTemperature();
    //Se envian datos por BT, en este caso estan fijos pero suponiendo que los guardas en variables seria
    if(millis()< (tiempo_ultima_medicion + 3000) )
    {
      BTserial.print("#"+String(temperature)+"/"+String(humedadPorcentaje)+";");
      tiempo_ultima_medicion = millis();
    }
     
    //BTserial.print("#20.3/13;"); 
    //delay(3000);
}

//Funcion que analiza el mensaje recibido por BT
void analizarMensaje(String m)
{
    //Si cumple con el protocolo se analiza el mensaje
    //"#modo/ventilacion/techoDescubierto/iluminacion/riego;"
    if(m.length()>1 && m.substring(0,1)=="#" && m.substring(m.length()-1,m.length())==";")
    { 
        //deja el mensaje sin # y ;
        String mensaje_aux=m.substring(1,m.length()-1);
        String modo_nuevo="";
        String ventilacion_nuevo="";
        String techoDesc_nuevo="";
        String iluminacion_nuevo="";
        String riego_nuevo;

        //baja a variables nuevas los valores del mensaje
        modo_nuevo = getValue(mensaje_aux,'/',0);
        ventilacion_nuevo = getValue(mensaje_aux,'/',1);
        techoDesc_nuevo = getValue(mensaje_aux,'/',2);
        iluminacion_nuevo = getValue(mensaje_aux,'/',3);
        riego_nuevo = getValue(mensaje_aux,'/',4);
        
        //verifica que valores cambiaron para ejecutar
        if(modo!=modo_nuevo)
        {
          //ejecuta los cambios correspondientes al modo
          modo=modo_nuevo;
          }

         if(ventilacion!=ventilacion_nuevo)
        {
          //ejecuta los cambios correspondientes a la ventilacion
          ventilacion=ventilacion_nuevo;
          ventilar();
          }

        if(techoDescubierto!=techoDesc_nuevo)
        {
          //ejecuta los cambios correspondientes al techo
          techoDescubierto=techoDesc_nuevo;
          descubrirTecho();
          }

          if(iluminacion!=iluminacion_nuevo)
        {
          //ejecuta los cambios correspondientes a la iluminacion
          iluminacion=iluminacion_nuevo;
          Iluminar();
          }

           if(riego!=riego_nuevo)
        {
          //ejecuta los cambios correspondientes a la bomba
          riego=riego_nuevo;
          regar();
          }

        Serial.println("Modo: "+modo);
        Serial.println("Ventilacion: "+ventilacion);
        Serial.println("Techo descubierto: "+techoDescubierto);
        Serial.println("Iluminacion: "+iluminacion);
        Serial.println("");

         
  }
}

//Simula un parse.
//Se pasa el mensaje, el caracter separador y el indice del valor que se necesita
String getValue(String data, char separator, int index)
{
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length()-1;

  for(int i=0; i<=maxIndex && found<=index; i++){
    if(data.charAt(i)==separator || i==maxIndex){
        found++;
        strIndex[0] = strIndex[1]+1;
        strIndex[1] = (i == maxIndex) ? i+1 : i;
    }
  }

  return found>index ? data.substring(strIndex[0], strIndex[1]) : "";
}

//Enciende el led
void Iluminar()
{
  //Serial.write("leyendo");
   if(iluminacion=="SI")
   {
      Serial.println("Encender Led");
      digitalWrite(pinLed, HIGH);  
   }
   else if(iluminacion=="NO")
   {
      Serial.println("Apagar Led");
      digitalWrite(pinLed, LOW);   // turn the LED on (HIGH is the voltage level)
   }
  }

void ventilar()
{
   if(ventilacion=="SI")
   {
      Serial.println("Encender COOLER");
      pinMode(PIN_RELE_COOLER, OUTPUT);
      digitalWrite(PIN_RELE_COOLER,LOW);  
   }
   else if(ventilacion=="NO")
   {
      Serial.println("Apagar COOLER");
      digitalWrite(PIN_RELE_COOLER,HIGH);
   }
}

void descubrirTecho()
{
  if(techoDescubierto=="SI")
   {
      Serial.println("Abrir Techo");
      servoIzq.write(90);
      servoDer.write(90);  
   }
   else if(techoDescubierto=="NO")
   {
      Serial.println("Cerrar Techo");
      servoIzq.write(0);
      servoDer.write(0);
   }
}

void regar()
{
  if(riego=="SI")
   {
      Serial.println("Encender Bomba");
      pinMode(PIN_RELE_BOMBA, OUTPUT);
      digitalWrite(PIN_RELE_BOMBA,LOW);  
   }
   else if(riego=="NO")
   {
      Serial.println("Apagar Bomba");
      digitalWrite(PIN_RELE_BOMBA,HIGH);
   }
}
/*void analizarDato(char c)
{
  //Serial.write("leyendo");
   if(c=='1')
   {
      Serial.println("Encender Led");
      digitalWrite(pinLed, HIGH);  
   }
   else if(c=='2')
   {
      Serial.println("Apagar Led");
      digitalWrite(pinLed, LOW);   // turn the LED on (HIGH is the voltage level)
   }
}*/
