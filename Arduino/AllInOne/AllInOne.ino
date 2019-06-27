#include <TinyGPS.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include "GL5528.h"
#include <RainSensorYL38.h>
#include <DHT22.h>
#include <EEPROM.h>
#include "I2Cdev.h"
#include "MPU6050.h"
#include "MPU6050Calibration.h"

#define DEBUG

// Sensors which are going to be used
TinyGPS myGPS;                              // GPS helper to get information from the sensor
SoftwareSerial myBT(10, 11); /* RX | TX */  // Bluetooth sensor
SoftwareSerial ss_gps(4, 3);                // GPS sensor
GL5528 gl5528(A1);                          // Light sensor
RainSensorYL38 RainSensor(A2);              // Rain sensor
DHT22 myDHT22(7);                           // Temperature and humidity sensor
MPU6050 mpu;                                // Accelerometer + gyroscope sensor

// Buttons necessaries to suggest an STOP, to warn of a dangerous situation and to reset the device
const int button_sos = 9;                   // Button to warn of a dangerous situation and to reset the device
const int button_stop = 8;                  // Button to suggest an STOP
bool SOS_pressed = false, stop_pressed = false;
bool b_first_time = true;
unsigned long reset_arduino_pressed_time;

// Variables used to read accelerometer and gyroscope data
int ax, ay, az;
int gx, gy, gz;

bool b_mpu_initiated = false;   // Has the accelerometer been started?
char dato = ' ';

// Variables used to control when data have to be read
unsigned long startMillisCritical, startMillisNotCritical;
unsigned long currentMillisCritical, currentMillisNotCritical;
unsigned long startGPSPeriod, currentGPSPeriod;
const unsigned long periodCritical = 50;
const unsigned long periodNotCritical = 5000;
const unsigned long gpsPeriod = 1000;

// Variables to store non-critical data
float temperature = -10000, humidity = -10000, lon = TinyGPS::GPS_INVALID_F_ANGLE, lat = TinyGPS::GPS_INVALID_F_ANGLE;
int light = -10000, rain = -10000;

// Read and write from/to EEPROM
struct EEPROM_Object {
  float ax_offset;
  float ay_offset;
  float az_offset;
  float gx_offset;
  float gy_offset;
  float gz_offset;
};
int address = 0;

void readGPSData(float* latitude, float* longitude);
void(* resetFunction) (void) = 0;

void setup() {
  // Initialize buttons and light sensor
  pinMode(A1, INPUT); // GL5528 Pin
  pinMode(button_sos, OUTPUT);
  pinMode(button_stop, OUTPUT);

  //  Initialize serial and bluetooth
#ifdef DEBUG
  Serial.begin(115200);
#endif
  myBT.begin(9600);
  myBT.print("AT+BAUD8");
  ss_gps.begin(4800);
  Wire.begin();

  // Initialize the accel+gyro
  mpu.initialize();
  if (mpu.testConnection()) b_mpu_initiated = true;

  // Turn on the led. The MPU6050 calibration is going to start
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);

  // Calibrate accel+gyro if it hasn't been configured before
  EEPROM_Object value;
  EEPROM.get(address, value);

  // The calibration is needed when the EEPROM doesn't have the accel+gyro calibration
  // stored on it
  if (isnan(value.az_offset)) {
    MPU6050Calibration calibration(mpu, &Serial);

    calibration.begin();

	  value = { 
      calibration.ax_offset,
      calibration.ay_offset,
      calibration.az_offset,
      calibration.gx_offset,
      calibration.gy_offset,
      calibration.gz_offset
    };
    EEPROM.put(address, value);
    
	  // Reset the Arduino
    resetFunction();
  } else {
    // If the calibration values are stored in the EEPROM, just use it
    mpu.setXAccelOffset(value.ax_offset);
    mpu.setYAccelOffset(value.ay_offset);
    mpu.setZAccelOffset(value.az_offset);
    mpu.setXGyroOffset(value.gx_offset);
    mpu.setYGyroOffset(value.gy_offset);
    mpu.setZGyroOffset(value.gz_offset);
  }

  // Turn off the led. The MPU6050 calibration has been finished
  digitalWrite(13, LOW);

  // To control when data have to be read
  startMillisCritical = millis();
  startMillisNotCritical = millis();
  startGPSPeriod = millis();
}

void loop() {
  // Buffers where the message that is going to be send by bluetooth is stored
  char msg_to_send[220] = {};
  char msg_copy[220];
  // To manage temperature sensor failures
  DHT22_ERROR_t errorCode;
  // At first, the buffers that store the BT message are empty
  strcpy(msg_to_send, "");
  strcpy(msg_copy, "");

  // Read the state of the buttons
  int button_sos_state = digitalRead(button_sos);
  int button_stop_state = digitalRead(button_stop);
  if (button_sos_state == HIGH) {
    SOS_pressed = true;
  } else if (button_stop_state == HIGH) {
    stop_pressed = true;
  }

  // Check how long the reset button has been pressed to know if it is 
  // necessary to clear the memory and restart the program
  if (button_sos_state == HIGH)
  {
    // Button has been pressed. Start to time counting
    if (b_first_time)
    {
      reset_arduino_pressed_time = millis();
      b_first_time = false;
    }
    
    // If the button has been pressed for more than 5 secs, erase EEPROM memory
    // and reset the arduino
    if(millis() - reset_arduino_pressed_time >= 5000)
    {
      eraseEEPROM();
      resetFunction();
    }
  } 

  // If the reset button was pressed and now it is released, reset default values
  if (button_sos_state == LOW && !b_first_time)
  {
    b_first_time = true;
  }

  // Check if it is necessary to read GPS data
  currentGPSPeriod = millis();
  if ((currentGPSPeriod - startGPSPeriod) >= gpsPeriod)
  {
    readGPSData(&lat, &lon);
    startGPSPeriod = currentGPSPeriod;
  }

  // Critical information is the information related with the accelerometer and the pressed buttons.
  // This information needs to be updated as soon as possible, so that the falls detection is possible
  currentMillisCritical = millis();
  if ((currentMillisCritical - startMillisCritical >= periodCritical) && b_mpu_initiated)
  {
    // Read raw acceleration and gyro data
    mpu.getAcceleration(&ax, &ay, &az);
    mpu.getRotation(&gx, &gy, &gz);
    // Add acceleration and gyro data to the BT message
    sprintf(msg_to_send, "ax: %d\nay: %d\naz: %d\ngx: %d\ngy: %d\ngz: %d\n", ax, ay, az, gx, gy, gz);

    // Add information about the pressed buttons to the BT message
    if (SOS_pressed)
    {
      strcat(msg_to_send, "Alert: true\nStop: false\n");
      SOS_pressed = false;
    } else if (stop_pressed)
    {
      strcat(msg_to_send, "Alert: false\nStop: true\n");
      stop_pressed = false;
    } else {
      strcat(msg_to_send, "Alert: false\nStop: false\n");
    }


    // Not critical information is the information related with temperature, humidity, rain,
    // light, etc. This information don't need to be updated every so often.
    currentMillisNotCritical = millis();
    if (currentMillisNotCritical - startMillisNotCritical >= periodNotCritical)
    {
      // Read temperature and humidity data
      errorCode = myDHT22.readData();
      if (errorCode == DHT_ERROR_NONE)
      {
        temperature = myDHT22.getTemperatureC();
        humidity = myDHT22.getHumidity();
        strcpy(msg_copy, msg_to_send);
        char temp[7];  dtostrf(temperature, 6, 2, temp);
        char humi[7];  dtostrf(humidity, 6, 2, humi);
        // Add temperature and humidity data to the BT message
        sprintf(msg_to_send, "%sTemperature: %s\nHumidity: %s\n", msg_copy, temp, humi);
      }
      else
      {
        // If there is an error, send temperature and humidity values as null
        strcat(msg_to_send, "Temperature: null\nHumidity: null\n");
      }

      strcpy(msg_copy, msg_to_send);
      char lati[10]; dtostrf(lat, 9, 5, lati);
      char longi[10]; dtostrf(lon, 9, 5, longi);
      // Add GPS data to the BT message
      sprintf(msg_to_send, "%slat: %s\nlon: %s\n", msg_copy, lati, longi);

      // Read light and rain data
      light = gl5528.getRawMeasure();
      rain = RainSensor.GetRawRainAmount();

      // Add light and rain data to the BT message
      strcpy(msg_copy, msg_to_send);
      sprintf(msg_to_send, "%sLight: %d\nRain: %d\n", msg_copy, light, rain);

      startMillisNotCritical = currentMillisNotCritical;
    }
    else  // Send the non-critical data which we have stored before
    {
      // If there is not necessary to update the values of the non critical sensors, we just send
      // the buffered values
      strcpy(msg_copy, msg_to_send);
      char temp[7];  dtostrf(temperature, 6, 2, temp);
      char humi[7];  dtostrf(humidity, 6, 2, humi);
      sprintf(msg_to_send, "%sTemperature: %s\nHumidity: %s\n", msg_copy, temp, humi);

      strcpy(msg_copy, msg_to_send);
      char lati[10]; dtostrf(lat, 9, 5, lati);
      char longi[10]; dtostrf(lon, 9, 5, longi);
      sprintf(msg_to_send, "%slat: %s\nlon: %s\n", msg_copy, lati, longi);
      strcpy(msg_copy, msg_to_send);
      sprintf(msg_to_send, "%sLight: %d\nRain: %d\n", msg_copy, light, rain);
    }

    // Print and send results
#ifdef DEBUG
    Serial.println(msg_to_send);
    Serial.flush();
#endif
    myBT.println(msg_to_send);
    startMillisCritical = currentMillisCritical;
  }

}

// This function clears the entire EEPROM memory
void eraseEEPROM()
{
  for (int i = 0; i< EEPROM.length(); i++) 
  {
    EEPROM.write(i, 255);  
  }
}

// This function reads data from the GPS sensor
void readGPSData(float* latitude, float* longitude)
{
  while (ss_gps.available())
  {
    char c = ss_gps.read();
    if (myGPS.encode(c))   // Did a new valid sentence come in?
    {
      myGPS.f_get_position(latitude, longitude);
    }
  }
}
