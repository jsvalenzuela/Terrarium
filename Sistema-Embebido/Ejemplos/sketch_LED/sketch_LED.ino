const int LED = 3;
int brillo;
void setup() {
  // put your setup code here, to run once:
pinMode(LED, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  for(brillo=0;brillo<256;brillo++)
  {
    analogWrite(LED,brillo);
    delay(10);
  }
 for(brillo=255;brillo>=0;brillo--)
  {
    analogWrite(LED,brillo);
    delay(10);
  }
  
}
