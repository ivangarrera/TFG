#include "Arduino.h"
#include "GL5528.h"

GL5528::GL5528(int pin) : pin_to_connect(pin)
{}

LightThresholds GL5528::getLightMeasure()
{
  int light_value = analogRead(pin_to_connect);

  if (light_value > 800)
  {
    return LightThresholds::LT_VERY_SUNNY;
  } else if (light_value > 450)
  {
    return LightThresholds::LT_SUNNY;
  } else if (light_value > 150)
  {
    return LightThresholds::LT_CLOUDY;
  } else
  {
    return LightThresholds::LT_NIGHT;
  }
}
