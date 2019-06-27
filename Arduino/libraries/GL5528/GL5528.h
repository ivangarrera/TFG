#ifndef GL5528_H
#define GL5528_H

typedef enum light_thresholds
{
  LT_VERY_SUNNY,
  LT_SUNNY,
  LT_CLOUDY,
  LT_NIGHT
} LightThresholds;

class GL5528 {
private:
  int pin_to_connect;
public:
  GL5528(int pin);

  GL5528(const GL5528&) = delete;
  GL5528& operator=(const GL5528&) = delete;

  LightThresholds getLightMeasure();
};

#endif //GL5528_H
