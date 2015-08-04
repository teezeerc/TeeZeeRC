/*
GNU GENERAL PUBLIC LICENSE Version 2, June 1991

author: Tomasz Zumbrzycki

Brain for Adruino part of TeeZeeRC RX
*/
#include <Servo.h>
#define MAX_TIME_NO_PACKET 5000000 //after 5 seconds failsafe is engaged
#define SERVOS 8

unsigned long timeLastPacket = 0;

uint8_t  failsafe[SERVOS] = {127,127,0,127,0,0,0,0};
uint8_t  g_pinsOut[SERVOS] = {2, 3, 4, 5, 6, 7, 8, 9}; // Output pins

Servo ch1;
Servo ch2;
Servo ch3;
Servo ch4;
Servo ch5;
Servo ch6;
Servo ch7;
Servo ch8;
Servo channels[SERVOS] = {ch1,ch2,ch3,ch4,ch5,ch6,ch7,ch8};

void setup()
{
  for (uint8_t i = 0;  i < SERVOS; ++i)
  {
    channels[i].attach(g_pinsOut[i]);
    channels[i].writeMicroseconds(map(failsafe[i], 0, 254, 1092, 1907)); //1092 and 1907 values adjusted for kk2 board. Values scale to range -100,100 on kk2
  }
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  delay(1000);
  while(1){
    if (Serial.find("5.1.4\r\n> "))
    {
      return;
    }
    else
    {
      Serial.println("node.restart()");
      Serial.flush();
      delay(2000);
    }
  }
}

void loop()
{
  static uint8_t buffer[12]; //11 bytes - packet body(first byte - type of packet, 8 bytes - channels positions, 2 bytes - CRS), 12 byte - FF appended by ESP8266 as a indicator of EOS
  static int length = 12;
  
  if (readline(Serial.read(), buffer, length) > 0) {
    //packet received
    if(packetCorrect(buffer,length)){
        timeLastPacket = micros();
        moveServos(buffer);
    }
  }else{
    //packet not received
    if((micros()-timeLastPacket)>MAX_TIME_NO_PACKET){
      //engage failsafe
      moveServosByFailsafe(failsafe);
    }
  }
}

void moveServos(uint8_t* buffer){
    for (uint8_t i = 0;  i < SERVOS; ++i)
    {
      channels[i].writeMicroseconds(map(buffer[i+1], 0, 254, 1092, 1907));
    }

    
}

void moveServosByFailsafe(uint8_t* failsafe){
    for (uint8_t i = 0;  i < SERVOS; ++i)
    {
      channels[i].writeMicroseconds(map(failsafe[i], 0, 254, 1092, 1907));
    }
}

/*
void printPacket(uint8_t* buffer, int length){
    dbgSerial.print("Received: >");
    for(int i=0;i<length;i++){
      dbgSerial.print(buffer[i],HEX);
      dbgSerial.print(" ");
    }
    dbgSerial.println("<");
}
*/

uint16_t fletcher16( uint8_t* buffer, int count )
{
   uint16_t sum1 = 0;
   uint16_t sum2 = 0;
   int index;
 
   for( index = 0; index < count; ++index )
   {
      sum1 = (sum1 + buffer[index]) % 255;
      sum2 = (sum2 + sum1) % 255;
   }
   return (sum2 << 8) | sum1;
}

boolean packetCorrect(uint8_t *buffer, int len){
    boolean result = false;  
    uint16_t tmp = fletcher16(buffer,len-3);
    uint8_t fl1 = (uint8_t)(tmp & 0xff);
    uint8_t fl2 = (uint8_t)(tmp>>8 & 0xff);
    if((buffer[len-2]==fl1)&&(buffer[len-3]==fl2)){
      result = true;
    }
    return result;
}

int readline(int readch, uint8_t *buffer, int len)
{
  static int pos = 0;
  int rpos;

  if (readch > -1) {
    switch (readch) {
      case 255: // FF - EOS indicator, appended by ESP8266 Lua server
        rpos = pos;
        pos = 0;  // Reset position index - ready for next time
        return rpos;
      default:
        if (pos < len-1) {
          buffer[pos++] = readch;
          buffer[pos] = 0;
        }
    }
  }
  // No end of line has been found, so return -1.
  return -1;
}


