#include <Servo.h>
 
Servo myservo;  // crea el objeto servo

int pos = 0;    // posicion del servo
int const pinServo = 12;
 
void setup() {
   myservo.attach(pinServo);  // vincula el servo al pin digital 11
}
 
void loop() {
   //varia la posicion de 0 a 180, con esperas de 15ms
   for (pos = 0; pos <= 180; pos += 1) 
   {
      myservo.write(pos);              
      delay(15);                       
   }
 
   //varia la posicion de 0 a 180, con esperas de 15ms
   for (pos = 180; pos >= 0; pos -= 1) 
   {
      myservo.write(pos);              
      delay(15);                       
   }
}