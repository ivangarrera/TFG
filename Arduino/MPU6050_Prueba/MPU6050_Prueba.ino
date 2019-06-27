#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"

const int SIZE = 20;

MPU6050 sensor;

float ax_vector[SIZE], ay_vector[SIZE], az_vector[SIZE];
float gx_vector[SIZE], gy_vector[SIZE], gz_vector[SIZE];

int ax, ay, az;
int gx, gy, gz;

float ax_mean = 0, ay_mean = 0, az_mean = 0;
float gx_mean = 0, gy_mean = 0, gz_mean = 0;

int counter = 0;

void calculate_means(int my_size);

void setup() {
  Serial.begin(57600);    //Iniciando puerto serial
  Wire.begin();           //Iniciando I2C  
  sensor.initialize();    //Iniciando el sensor

  if (sensor.testConnection()) Serial.println("Sensor iniciado correctamente");
  else Serial.println("Error al iniciar el sensor");
}

void loop() {

  if (counter == SIZE)
  {
    counter = 0;
    calculate_means(SIZE);
    Serial.print("a[x y z](m/s2) g[x y z](deg/s):\t");
    Serial.print(ax_mean); Serial.print("\t");
    Serial.print(ay_mean); Serial.print("\t");
    Serial.print(az_mean); Serial.print("\t");
    Serial.print(gx_mean); Serial.print("\t");
    Serial.print(gy_mean); Serial.print("\t");
    Serial.println(gz_mean);
  }
  // Leer las aceleraciones y velocidades angulares
  sensor.getAcceleration(&ax, &ay, &az);
  sensor.getRotation(&gx, &gy, &gz);

  ax_vector[counter] = ax * (9.81/16384.0);
  ay_vector[counter] = ay * (9.81/16384.0);
  az_vector[counter] = az * (9.81/16384.0);
  gx_vector[counter] = gx * (250.0/32768.0);
  gy_vector[counter] = gy * (250.0/32768.0);
  gz_vector[counter] = gz * (250.0/32768.0);
  
  delay(100);
  counter++;
}

void calculate_means(int my_size)
{
  for (int i = 0; i < my_size; i++)
  {
    ax_mean += ax_vector[i];
    ay_mean += ay_vector[i];
    az_mean += az_vector[i];
    gx_mean += gx_vector[i];
    gy_mean += gy_vector[i];
    gz_mean += gz_vector[i];
  }
  ax_mean /= my_size;
  ay_mean /= my_size;
  az_mean /= my_size;
  gx_mean /= my_size;
  gy_mean /= my_size;
  gz_mean /= my_size;
}
