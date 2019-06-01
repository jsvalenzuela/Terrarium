#include "DHT.h"
#define DHTTYPE DHT11
const int RELE = 8;
const int DHTPIN =12;
DHT dht(DHTPIN,DHTTYPE);
boolean flag;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
dht.begin();
flag = false;
}

void loop() {
  // put your main code here, to run repeatedly:
  float humedad = dht.readHumidity();
  float temperature = dht.readTemperature();
  Serial.println(temperature);
  if(temperature>30)
  {
    boolean aux = temperature>30;
    Serial.println("Resultado Condicion:");
    Serial.print(aux);
     Serial.println("Intento Encencer Cooler");
    if(flag==false)
    {
        pinMode(RELE, OUTPUT);
       digitalWrite(RELE,LOW);
       flag=true;
       Serial.println("Enciende Cooler");
    }
  }
  else
  {
    Serial.println("Intento Apagar Cooler");
    if(flag==true)
    {
     digitalWrite(RELE,HIGH);
     flag=false;
     Serial.println("Apaga Cooler");
    }
  }
/*digitalWrite(RELE,HIGH);
delay(10000);
digitalWrite(RELE,LOW);
delay(10000);*/

}
