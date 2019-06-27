#include "Arduino.h"
#include "RainSensorYL38.h"

RainSensorYL38::RainSensorYL38(int pin) : pin(pin), threshold(500)
{
  pinMode(pin, INPUT);
}

RainAmount RainSensorYL38::GetRainAmount()
{
  int value = analogRead(pin);
  if (value < threshold && value > threshold - 200)
  {
    return RainAmount::RA_LOW_RAIN;
  }
  else if (value <= threshold - 200 && value > threshold - 400)
  {
    return RainAmount::RA_MODERATE_RAIN;
  }
  else if (value <= threshold - 400)
  {
    return RainAmount::RA_HEAVY_RAIN;
  }
  return RainAmount::RA_NO_RAIN;
}
