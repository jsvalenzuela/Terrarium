#include <LiquidCrystal_I2C.h> // Libreria LCD_I2C
#include<Wire.h>
#include "DHT.h" // Libreria Sensor temperatura dth11
#define DHTTYPE DHT11

// **** PINES DIGITALES  ****
const int PIN_TEMPERATURA =7 ;
const int PIN_RELE_COOLER = 4 ;
const int PIN_RELE_BOMBA = 8 ;


// **** PINES ANALOGICOS ****
const int PIN_HUMEDAD = A0;

// **** PINES PWM ****
const int PIN_LED = 3 ;

DHT dht(PIN_TEMPERATURA,DHTTYPE);
LiquidCrystal_I2C lcd(0x27,16,2);

// Variables globales
boolean bomba = false ;

void setup() {
  
  Serial.begin(9600);
  dht.begin();
  //pinMode(PIN_RELE_COOLER, OUTPUT);
  //==========Seteo Inicial para el setup=========//
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.display();
  //=============================================//
  pinMode(PIN_LED, OUTPUT);
  
}


void loop() {
  
  float temperature = dht.readTemperature();
  // # rango de valores # 0 ~300 muy humedo # 300~700 intermedio # 700~1023 muy seco*
  // FALTA VERLO EN PORCENTAJE
  int humedad = analogRead(PIN_HUMEDAD);
  //int humedadPorcentaje = map(analogRead(PIN_HUMEDAD), 1023, 0, 0, 100);
  
  /*Mostrar temperatura y humedad de la tierra*/
  /*lcd.setCursor(0,0);
  lcd.print("Temp: "+String(temperature)+"C");
  lcd.setCursor (0,1);
  lcd.print("Humedad: "+String(humedad));
*/
  Serial.print("Temperatura: ");
  Serial.println(temperature);
  Serial.print("Humedad: ");
  Serial.println(humedad);
  Serial.println(bomba);      
  
  /*if(temperature>25){
        pinMode(PIN_RELE_COOLER, OUTPUT);
        digitalWrite(PIN_RELE_COOLER,LOW);
        Serial.print("Temperatura: ");
        Serial.println(temperature);        
        
        lcd.setCursor(0,0);
        lcd.print("COOLER");
        lcd.setCursor (0,1);
        lcd.print("ENCENDIDO");
    }
    else{
      
        Serial.print("Temperatura: ");
        Serial.println(temperature);
        
        digitalWrite(PIN_RELE_COOLER,HIGH);
        lcd.setCursor(0,0);
        lcd.print("COOLER");
        lcd.setCursor (0,1);
        lcd.print("APAGADO"); 
  }
*/
  if(humedad > 700 ){

      if(bomba == false){
      pinMode(PIN_RELE_BOMBA, OUTPUT);
      digitalWrite(PIN_RELE_BOMBA,LOW);
      bomba = true ;
      lcd.setCursor(0,0);
      lcd.print("BOMBA");
      lcd.setCursor (0,1);
      lcd.print("ENCENDIDA");
      //prenderLed();
      }

      
      
  }
  else if(humedad < 400){
    
      Serial.print("muy mojado");
      if(bomba == true ){
      
      digitalWrite(PIN_RELE_BOMBA,HIGH);

      lcd.setCursor(0,0);
      lcd.print("BOMBA");
      lcd.setCursor (0,1);
      lcd.print("APAGADA"); 
      bomba=false;
      }
      
  }
  
}

void prenderLed(){

  int i;
  for  (i=0;i<256; i++){
      analogWrite(PIN_LED,i);
      delay(10);
  }
  
  for  (i=255;i>=0; i--){
      analogWrite(PIN_LED,i);
      delay(10);
  }
  
}
