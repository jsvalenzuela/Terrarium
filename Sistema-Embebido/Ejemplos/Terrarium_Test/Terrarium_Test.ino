//-------------------------------------Proyecto Integrando Componentes y Sensores---------------------------------//
#include <Servo.h>
//Defino Constantes Gobales (Constantes van en mayuscula)
const int PIN_SERVO_IZQ = 2;//digital
const int PIN_SERVO_DER = 3;//digital
const int PIN_LLUVIA = 9;// usamos dig y su sensibilidad se ajusta con el potenciometro, por tanto es llueve o no llueve. El analogico no es de interes xq carece de precision para determinar la cantidad de agua
const int PIN_HUMEDAD = A0;//similar al sensor de lluvia. Uso Analogico porque si me interesa un rango de valores mas amplios en la humedad.
const int DELAY_SERVOS = 8000;//Espero 8 segundos para abrir o cerrar techo

//Defino variables Goblabes (para variables y demas uso camelCase)
Servo servoIzq;
Servo servoDer;
bool skyClose;
bool regar;

unsigned long t_lastTime = 0;///al inicio no hubo ultima medicion
const int esperaHumedad = 5000;/// dejo techo abierto por  segundos, sino esta humedo pregunto si sigue lloviendo


void setup() {
Serial.begin(9600); 
servoIzq.attach(PIN_SERVO_IZQ);
servoDer.attach(PIN_SERVO_DER);
servoIzq.write(0);//Seteo pos inicial a techo cerrado
servoDer.write(180);//Seteo pos inicial a techo cerrado
skyClose = true; // el techo esta cerrado inicialmente

pinMode(PIN_LLUVIA, INPUT);  //definir pin(solo usado en digitales) como entrada

delay(2000);
}

void loop() {
int humedad;
int llueve;
regar = true; //Se busca regar inicialmente
humedad = analogRead(PIN_HUMEDAD);
if(humedad >= 600){
  Serial.println("El SUELO ESTA SECO y voy a regar naturalmente con lluvia");
  llueve = digitalRead(PIN_LLUVIA);  //lectura digital de pin

  while(regar){
        Serial.println("LLueve??");
        if (llueve == LOW){
                Serial.println("Detectada lluvia");
                    if(skyClose == true){
                    abrirTecho(servoIzq, servoDer);
                    }
         }
        else{
             Serial.println("Cielo Despejado");
              if(skyClose == false){
                      cerrarTecho(servoIzq, servoDer);
               }
        }
    llueve = digitalRead(PIN_LLUVIA);
  }
}
Serial.println("No es necesario Regar");
}


//Implementaciones de Funciones
void abrirTecho(Servo servoIzq, Servo servoDer){
   //varia la posicion de 0 a 180, con esperas de 15ms
   unsigned long t_last = millis();
   Serial.write("Abriendo Techo");
   servoIzq.write(180);
   servoDer.write(0);
   //delay(15000);
   myDelay(t_last, DELAY_SERVOS);
   skyClose = false;
}

void cerrarTecho(Servo servoIzq, Servo servoDer){
  //varia la posicion de 0 a 180, con esperas de 15ms
  unsigned long t_last = millis();
  Serial.write("Cerrando Techo");
  servoIzq.write(0);
  servoDer.write(180);
  //delay(15000);
  myDelay(t_last, DELAY_SERVOS);                      
  skyClose = true;
  regar = false;
}

void myDelay(unsigned long t_last, int periodoAEsperar ){
  while(millis() < (t_last+ periodoAEsperar) ){                    
   }
}
