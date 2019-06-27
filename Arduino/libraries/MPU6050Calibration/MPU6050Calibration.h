#ifndef _MPU6050Calibration_H
#define _MPU6050Calibration_H

class MPU6050;

typedef enum MPU6050Calibration_state
{
  ST_FIRST_TIME,
  ST_CALCULATE_OFFSETS,
  ST_FINISHED
} MPU6050CalibrationState;

class MPU6050Calibration
{
private:
  // Amount of readings used to average, make it higher to get more precision but sketch will be slower  (default:1000)
  const int buffersize = 1000;
  // Acelerometer error allowed, make it lower to get more precision, but sketch may not converge  (default:8)
  const int acel_deadzone = 8;
  // Giro error allowed, make it lower to get more precision, but sketch may not converge  (default:1)
  const int giro_deadzone = 1;
  Print* printer;

  MPU6050Calibration_state state;
  MPU6050& mpu6050_sensor;
  int ax, ay, az;
  int gx, gy, gz;

public:
  int mean_ax, mean_ay, mean_az;
  int mean_gx, mean_gy, mean_gz;
  float ax_offset, ay_offset, az_offset;
  float gx_offset, gy_offset, gz_offset;

public:
  MPU6050Calibration(MPU6050& mpu6050_sensor, Print &print);
  void CalibrateMPU6050();

private:
  void TakeAMeasurement();
  void Calibrate();
};

#endif //_MPU6050Calibration_H
