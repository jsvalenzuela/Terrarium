const int humedadPin = A5;
const int releBomba = 8;
boolean bomba = false ;

void setup() {
  
  Serial.begin(9600);
  pinMode(releBomba, OUTPUT);
}

void loop() {
 
  // # rango de valores # 0 ~300 muy humedo # 300~700 intermedio # 700~1023 muy seco*
  int humedad = analogRead(humedadPin);
  Serial.print("Humedad: ");
  Serial.println(humedad);
  
  /*int lecturaPorcentaje = map(humedad, 1023, 0, 0, 100);
  Serial.print("La humedad es del: ");
  Serial.println(lecturaPorcentaje);
  */
  Serial.print(bomba);
  
  if(humedad > 700){

      if(bomba == false){
        
        digitalWrite(releBomba, LOW);   
        bomba = true ;
      }
      
  }
  else if(humedad < 400){

        Serial.print("muy mojado");
        digitalWrite(releBomba, HIGH);
        Serial.print(bomba);
        bomba=false;
  }

}
