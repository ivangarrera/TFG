#include <RainSensorYL38.h>

RainSensorYL38 RainSensor(A0);

void setup() {
  Serial.begin(9600);
}

void loop() {
  RainAmount rainamount = RainSensor.GetRainAmount();
  switch (rainamount)
  {
    case RainAmount::RA_NO_RAIN:
      Serial.println("No Rain");
      break;
    case RainAmount::RA_LOW_RAIN:
      Serial.println("Low rain");
      break;
    case RainAmount::RA_MODERATE_RAIN:
      Serial.println("Moderate rain");
      break;
    case RainAmount::RA_HEAVY_RAIN:
      Serial.println("Heavy rain");
      break;
  }
}
