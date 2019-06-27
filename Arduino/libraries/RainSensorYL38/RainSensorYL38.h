/*
  RainSensorYL38.h - Library to use YL-38 sensor.
  Created by Ivan Garcia, 2018.
  Released into the public domain.
*/
#ifndef RainSensor_h
#define RainSensor_h

#include "Arduino.h"


enum RainAmount {
  RA_NO_RAIN,
  RA_LOW_RAIN,
  RA_MODERATE_RAIN,
  RA_HEAVY_RAIN
};

class RainSensorYL38
{
  public:
    RainSensorYL38(int pin);
    RainAmount GetRainAmount();
    inline void modifyThreshold(int threshold) { this->threshold = threshold; }
  private:
    int pin;
    int threshold;
};


#endif
