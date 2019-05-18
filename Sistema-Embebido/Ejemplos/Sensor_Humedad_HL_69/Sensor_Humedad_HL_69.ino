#include <LiquidCrystal_I2C.h> // Libreria LCD_I2C
#include<Wire.h>

LiquidCrystal_I2C lcd(0x27,16,2);

const int sensorPin = A0;
int lectura;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  ///setup para el lcd
  lcd.init();
  lcd.backlight();
  lcd.clear();
}

void loop() {
  // put your main code here, to run repeatedly:
  lectura = analogRead(sensorPin);
  Serial.print("La lectura es: ");
  Serial.println(lectura);
  if(lectura >= 1000){
    Serial.println(">> El SENSOR ESTA DESCONECTADO O FUERA DEL SUELO");
  }
  else if(lectura <1000 && lectura >= 600){
    Serial.println(">> El SUELO ESTA SECO");
  }
  else if(lectura <600 && lectura >= 370){
    Serial.println(">> El SUELO ESTA HUMEDO");
  }
  else if(lectura < 370){
    Serial.println(">> El SENSOR ESTA PRACTICAMENTE EN AGUA");
  }
  ///delay(1000);
  int lecturaPorcentaje = map(lectura, 1023, 0, 0, 100); 
  Serial.print("La humedad es del: ");
  Serial.print(lecturaPorcentaje);
  Serial.println("%");






  //LCD
  lcd.setCursor(0,0);
  lcd.print("H:");


  lcd.setCursor(2,0);
  lcd.print(" ");

  lcd.setCursor(3,0);
  lcd.print(lecturaPorcentaje);

   lcd.setCursor(7,0);
  lcd.print("%");
   
  delay(1000);
}
