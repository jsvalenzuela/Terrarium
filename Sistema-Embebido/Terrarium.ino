#include <Servo.h>
#include <LiquidCrystal_I2C.h>
#include "DHT.h"

#define DHTTYPE DHT11
//-------------------Definicion de Pines---------------------//
//*Pines Analogicos
#define PIN_HUMEDAD A1
//*Pines Digitales
#define PIN_LED 6
#define PIN_LED_B 11
#define PIN_SERVO_IZQ 5
#define PIN_SERVO_DER 3
#define DHTPIN 7 
#define PIN_RELE_COOLER 4
#define PIN_LLUVIA 2
#define PIN_RELE_BOMBA 8
//------------Otras declaraciones Globales---------------//
Servo servoIzq;
Servo servoDer;
unsigned long tiempo_ultimo_envio = 0;
unsigned long tiempo_inicio_apertura_techo = 0;
unsigned long tiempo_ultimo_blink = 0;
String modo = "";
int stateCooler = 0;
int stateBomba = 0;
int stateTecho = 0;
int stateLuz = 0;
int flagUsoAutomatico = 0;
String mnj = "";
String sCooler = "V:NO";
String sRiego = "R:NO";
String sTecho = "T:NO";
byte intensidadLeds = 0;

DHT dht(DHTPIN, DHTTYPE);
LiquidCrystal_I2C lcd(0x27,16,2);


String ventilacion="";
String techoDescubierto="";
String iluminacion="";
String riego="";

void setup()
{
  Serial.begin(9600);
  pinMode(PIN_LED, OUTPUT);
  pinMode(PIN_HUMEDAD, INPUT);
  dht.begin();

  servoIzq.attach(PIN_SERVO_IZQ);
  servoDer.attach(PIN_SERVO_DER);
  cerrarTecho();

 lcd.init();
 lcd.backlight();
 lcd.clear();
 lcd.setCursor(0,0);
 float t = dht.readTemperature();
 int h = map(analogRead(PIN_HUMEDAD), 0, 1023, 100, 0);
 if(!isnan(t))
  {
    Serial.print("#"+String(t)+"/"+String(h)+";");
    tiempo_ultimo_envio = millis();
  }
 printOnLCD("Welcome To", "Terrarium...");
}

void loop() {

 //--------------------------------------------ENVIO DE DATOS-------------------------------------------------------------------------// 
  float t = dht.readTemperature();
  int h = map(analogRead(PIN_HUMEDAD), 0, 1023, 100, 0);
  unsigned long tiempo_nuevo_envio = millis() - tiempo_ultimo_envio;
  if(tiempo_nuevo_envio > 2000 && !isnan(t))// Cada 10 minutos envio lecturas osea 600000ms
  {
    Serial.print("#"+String(t)+" C"+"/"+String(h)+" %"+";");
    tiempo_ultimo_envio = millis();
  }
//------------------------------------------RECEPCION DE DATOS----------------------------------------------------------------------//
  if (Serial.available() >1)
    { 
      mnj = Serial.readString();
      identificarModo(mnj);
         if(modo.equals("Manual"))
         {
          activarModoManual(mnj);
         }
     else if(modo.equals("Automatico"))
         {
          flagUsoAutomatico = 0;
          activarModoAutomatico(mnj);
         }
    }
  else if(modo.equals("Automatico"))
  {
        activarModoAutomatico(mnj);
  }
}

void identificarModo(String m)
{
  if(m.startsWith("x") )
      {
        m.remove(0,1);
      }
    if(m.length() > 1 && m.startsWith("#") && m.endsWith(";"))
    { 
        mnj=m.substring(1,m.length()-1);
        modo = getValue(mnj,'/',0);
    }
}

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
void printOnLCD(String linea1, String linea2)
{
  lcd.noDisplay();
  if(linea1.length() <= 16 && linea2.length() <= 16)
  {
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print(linea1); 
    lcd.setCursor (0,1);
    lcd.print(linea2);
    lcd.display();
  }
  
}

void activarModoAutomatico(String m) 
 {
  String linea1 = "Modo:Automatico";
  String linea2 = "";
  if(stateCooler == 1)
  {
    sCooler = "V:SI ";
  }
  if(stateBomba == 1)
  {
    sRiego = "R:SI";
    pwmHighLeds();
  }
  else
  {
    pwmLowLeds();
  }
  if(stateTecho == 1)
  {
    sTecho = "T:SI";
  }

  int currentHumedad = map(analogRead(PIN_HUMEDAD), 0, 1023, 100, 0);
  float currentTemperature = dht.readTemperature();

  int humedadServicio = (getValue(m,'/',1)).toInt();
  float temperaturaServicio = (getValue(m,'/',2)).toFloat();
  float upperLimitT = (temperaturaServicio*1.05)>48?48:(temperaturaServicio*1.05);
  float lowerLimitT = temperaturaServicio*0.95;
  int upperLimitH = (humedadServicio*1.05)>98?98:(humedadServicio*1.05);
  int lowerLimitH = humedadServicio*0.95;
  
  if(stateCooler == 0 && currentTemperature > upperLimitT)
  {
      encenderCooler();
      sCooler = "V:SI ";
      flagUsoAutomatico = 0;
  }
  else if(stateCooler == 1 && currentTemperature < lowerLimitT)
  {
      apagarCooler();
      sCooler = "V:NO ";
      flagUsoAutomatico = 0;
  }

int respuestaUsuario = (getValue(m,'/',3)).toInt();
  switch(respuestaUsuario)
  {
    case 1 :
             if(stateBomba == 0 && currentHumedad < lowerLimitH)
             {
               encenderBombaAgua();
               sRiego = "R:SI";
               flagUsoAutomatico = 0;
             }
             else if(stateBomba == 1 && currentHumedad > upperLimitH)
             {
               apagarBombaAgua();
               sRiego = "R:NO";
               flagUsoAutomatico = 0;
             }
             break;
    case 2 :
             int isRaining = digitalRead(PIN_LLUVIA);
             if(isRaining==LOW && stateTecho == 0 && currentHumedad < lowerLimitH)
             {
               abrirTecho();
               tiempo_inicio_apertura_techo = millis();
               sTecho = "T:SI";
               flagUsoAutomatico = 0;
             }
             else if(stateTecho == 1 && currentHumedad > upperLimitH)
             {
              unsigned long tiempo_hasta_cerrar_techo = millis() - tiempo_inicio_apertura_techo;
              if(tiempo_hasta_cerrar_techo > 3000 )
              {
               cerrarTecho();
               sTecho = "T:NO";
               flagUsoAutomatico = 0;
              }
             }
             break;
  }
  if(flagUsoAutomatico == 0)
  {
    linea2 = sCooler+" "+sRiego+" "+sTecho;
    printOnLCD(linea1, linea2);
    flagUsoAutomatico = 1;
  }
 }

void activarModoManual(String m)
{
  ///Como se valido la cadena del mnj antes, entonces a priori se como continua la cadena para este modo
  String newVentilacion = getValue(m,'/',1);
  String newTechoDesc = getValue(m,'/',2);
  String newIluminacion = getValue(m,'/',3);
  String newRiego = getValue(m,'/',4);

  if(ventilacion != newVentilacion)
    {
     ventilacion = newVentilacion;
       ventilar();
    }

  if(techoDescubierto != newTechoDesc)
    {
     techoDescubierto = newTechoDesc;
       descubrirTecho();
    }

  if(iluminacion != newIluminacion)
    {
     iluminacion = newIluminacion;
       iluminar();
    }

  if(riego != newRiego)
    {
     riego = newRiego;
       regar();
    }
  String setDeEjecucion;
  if(newRiego.length() > 1)
  {
    setDeEjecucion = "V:"+ventilacion.substring(0,1)+" T:"+techoDescubierto.substring(0,1)+" I:"+iluminacion.substring(0,1)+" R:"+riego.substring(0,1);
  }
  else
  {
    setDeEjecucion = "V:"+ventilacion.substring(0,1)+" T:"+techoDescubierto.substring(0,1)+" I:"+iluminacion.substring(0,1)+" R:N";
  }
  printOnLCD("Modo: Manual", setDeEjecucion);
}
void pwmHighLeds()
{
  unsigned long tiempo_nuevo_blink = millis() - tiempo_ultimo_blink;
  if( (intensidadLeds+1) <= 256)
  {
    if(tiempo_nuevo_blink > 100)
    {
      analogWrite(PIN_LED,intensidadLeds);
      analogWrite(PIN_LED_B,intensidadLeds);
      tiempo_ultimo_blink = millis();
      intensidadLeds = (intensidadLeds+1) <= 255?(intensidadLeds+1):255 ;
    }
  }
}
void pwmLowLeds()
{
  unsigned long tiempo_nuevo_blink = millis() - tiempo_ultimo_blink;
  if( (intensidadLeds-1) >= -1  )
  {
    if(tiempo_nuevo_blink > 100)
    {
      analogWrite(PIN_LED,intensidadLeds);
      analogWrite(PIN_LED_B,intensidadLeds);
      tiempo_ultimo_blink = millis();
      intensidadLeds = (intensidadLeds-1) >= 0?(intensidadLeds-1):0;
    }
  }
}
//--------------------------Acciones Modo Manual------------------------------------//
void iluminar()
{ 
   if(iluminacion == "SI")
   {
      encenderLuz();  
   }
   else if(iluminacion == "NO")
   {
      apagarLuz();
   }
 }

void ventilar()
{
   if(ventilacion == "SI")
   {
      encenderCooler();  
   }
   else if(ventilacion == "NO")
   {
      apagarCooler();
   }
}

void descubrirTecho()
{
  if(techoDescubierto == "SI")
   {
      abrirTecho();
   }
   else if( techoDescubierto == "NO")
   {
      cerrarTecho();
   }
}

void regar()
{
  if(riego == "SI")
   {
      encenderBombaAgua();
   }
   else if(riego == "NO")
   {
     apagarBombaAgua();
   }
}
//-----------------------Funciones Primitivas del Terrario-----------------------------//
void abrirTecho()
{
   stateTecho = 1;
  servoIzq.write(5);
  servoDer.write(5);  
}

void cerrarTecho()
{
  stateTecho = 0;
  servoIzq.write(85);
  servoDer.write(85);
}

void encenderBombaAgua()
{
  stateBomba = 1;
  pinMode(PIN_RELE_BOMBA, OUTPUT);
  digitalWrite(PIN_RELE_BOMBA,LOW);
  
}
void apagarBombaAgua()
{
  stateBomba = 0;
  digitalWrite(PIN_RELE_BOMBA,HIGH);
}


void encenderCooler()
{
  stateCooler = 1;
  pinMode(PIN_RELE_COOLER, OUTPUT);
  digitalWrite(PIN_RELE_COOLER,LOW);
  
}

void apagarCooler()
{
  stateCooler = 0;
  digitalWrite(PIN_RELE_COOLER,HIGH);
}

void encenderLuz()
{
  digitalWrite(PIN_LED, HIGH);
  digitalWrite(PIN_LED_B, HIGH);
  stateLuz = 1;
}

void apagarLuz()
{
  digitalWrite(PIN_LED, LOW);
  digitalWrite(PIN_LED_B, LOW);
  stateLuz = 0;
}
