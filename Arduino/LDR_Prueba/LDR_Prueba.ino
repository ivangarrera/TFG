#include "GL5528.h"


GL5528 gl5528(A0);

void setup(){
 pinMode(A0, INPUT);// Set pResistor - A0 pin as an input (optional)
 Serial.begin(9600);
}

void loop(){
  if (gl5528.getLightMeasure() == LightThresholds::LT_VERY_SUNNY)
  {
    Serial.println("Very Sunny"); 
  }
  
  delay(500); //Small delay
}
