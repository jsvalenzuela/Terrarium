#include <LiquidCrystal_I2C.h> // Libreria LCD_I2C
#include<Wire.h>
#include "DHT.h"
#define DHTTYPE DHT11
const int DHTPIN =7;
const int RELE = 8;
const int PIN_HUMEDAD = A0;

DHT dht(DHTPIN,DHTTYPE);
LiquidCrystal_I2C lcd(0x27,16,2);
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  dht.begin();
  //pinMode(RELE, OUTPUT);
  //==========Seteo Inicial para el setup=========//
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.display();
  //=============================================//
}

void loop() {
float temperature = dht.readTemperature();
///int humedad = analogRead(PIN_HUMEDAD);
int humedadPorcentaje = map(analogRead(PIN_HUMEDAD), 1023, 0, 0, 100);
if(temperature>25){
      pinMode(RELE, OUTPUT);
      digitalWrite(RELE,LOW);
      Serial.print("Temperatura:");
      Serial.println(temperature);
      lcd.setCursor(0,0);
      lcd.print("COOLER");
      lcd.setCursor (0,1);
      lcd.print("ENCENDIDO");
  }
  else{
       Serial.print("Temperatura:");
       Serial.println(temperature);
       digitalWrite(RELE,HIGH);
       lcd.setCursor(0,0);
       lcd.print("COOLER");
       lcd.setCursor (0,1);
       lcd.print("APAGADO"); 
 }
}
