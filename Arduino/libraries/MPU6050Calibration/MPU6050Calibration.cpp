#include "Arduino.h"
#include "MPU6050Calibration.h"
#include "MPU6050.h"

MPU6050Calibration::MPU6050Calibration(MPU6050& mpu6050_sensor, Print &print)
: ax(0), ay(0), az(0), gx(0), gy(0), gz(0), mean_ax(0), mean_ay(0), mean_az(0),
mean_gx(0), mean_gy(0), mean_gz(0)
{
  this->mpu6050_sensor = mpu6050_sensor;
  printer = &print;
  state = MPU6050CalibrationState::ST_FIRST_TIME;

  // Reset offsets
  mpu6050_sensor.setXAccelOffset(0);
  mpu6050_sensor.setYAccelOffset(0);
  mpu6050_sensor.setZAccelOffset(0);
  mpu6050_sensor.setXGyroOffset(0);
  mpu6050_sensor.setYGyroOffset(0);
  mpu6050_sensor.setZGyroOffset(0);
}

void MPU6050Calibration::CalibrateMPU6050()
{
  printer->println("CalibrateMPU6050");
  while(1)
  {
    if (state == MPU6050CalibrationState::ST_FIRST_TIME)
    {
      printer->println("First measures");
      TakeAMeasurement();
      state = 1;
      delay(1000);
    }
    else if (state == MPU6050CalibrationState::ST_CALCULATE_OFFSETS)
    {
      printer->println("Calculating offsets...");
      Calibrate();
      state = MPU6050CalibrationState::ST_FINISHED;
      delay(1000);
    }
    else if (state == MPU6050CalibrationState::ST_FINISHED)
    {
      printer->println("Calibration done.");
      TakeAMeasurement();
      break;
    }
  }
}

void MPU6050Calibration::TakeAMeasurement()
{
  long i = 0, buff_ax = 0, buff_ay = 0, buff_az = 0, buff_gx = 0, buff_gy = 0, buff_gz = 0;

  while (i < (buffersize + 101))
  {
    // Get accelerometer and gyroscope measures
    mpu6050_sensor.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);

    // The first 100 measures are discarded
    if (i > 100 && i <= (buffersize + 100))
    {
      buff_ax += ax;   buff_ay += ay;   buff_az += az;
      buff_gx += gx;   buff_gy += gy;   buff_gz += gz;
    }

    // Obtain the mean of the measures
    if (i == (buffersize + 100)){
      mean_ax = buff_ax / buffersize;
      mean_ay = buff_ay / buffersize;
      mean_az = buff_az / buffersize;
      mean_gx = buff_gx / buffersize;
      mean_gy = buff_gy / buffersize;
      mean_gz = buff_gz / buffersize;
    }
    i++;
    delay(2); //Needed so we don't get repeated measures
  }
}

void MPU6050Calibration::Calibrate(){
  ax_offset = -mean_ax / 8;
  ay_offset = -mean_ay / 8;
  az_offset = (16384 - mean_az) / 8;

  gx_offset = -mean_gx / 4;
  gy_offset = -mean_gy / 4;
  gz_offset = -mean_gz / 4;
  while (1){
    int ready = 0;
    mpu6050_sensor.setXAccelOffset(ax_offset);
    mpu6050_sensor.setYAccelOffset(ay_offset);
    mpu6050_sensor.setZAccelOffset(az_offset);

    mpu6050_sensor.setXGyroOffset(gx_offset);
    mpu6050_sensor.setYGyroOffset(gy_offset);
    mpu6050_sensor.setZGyroOffset(gz_offset);

    TakeAMeasurement();

    // Check if all the offsets are ok. If only one of the offset is not ok,
    // correct the offset and take measures again
    if (abs(mean_ax) <= acel_deadzone) ready++;
    else ax_offset = ax_offset - mean_ax / acel_deadzone;

    if (abs(mean_ay) <= acel_deadzone) ready++;
    else ay_offset = ay_offset-mean_ay / acel_deadzone;

    if (abs(16384 - mean_az) <= acel_deadzone) ready++;
    else az_offset = az_offset + (16384 - mean_az) / acel_deadzone;

    if (abs(mean_gx) <= giro_deadzone) ready++;
    else gx_offset = gx_offset - mean_gx / (giro_deadzone + 1);

    if (abs(mean_gy) <= giro_deadzone) ready++;
    else gy_offset = gy_offset - mean_gy / (giro_deadzone + 1);

    if (abs(mean_gz) <= giro_deadzone) ready++;
    else gz_offset = gz_offset-mean_gz / (giro_deadzone + 1);

    printer->print("Number Of correct offsets: ");
    printer->println(ready);
    // When all the offsets are correct, ready will have value=6
    if (ready == 6) break;
  }
}
