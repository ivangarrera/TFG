#include <DHT22.h>

// Data wire is plugged into port 7 on the Arduino
// Connect a 4.7K resistor between VCC and the data pin (strong pullup)
#define DHT22_PIN 7

// Setup a DHT22 instance
DHT22 myDHT22(DHT22_PIN);

void setup(void)
{
  Serial.begin(9600);
}

void loop()
{ 
  DHT22_ERROR_t errorCode;
  
  // The sensor can only be read from every 1-2s, and requires a minimum
  // 2s warm-up after power-on.
  delay(2000);
  
  errorCode = myDHT22.readData();
  if (errorCode == DHT_ERROR_NONE)
  {
      Serial.print("Temperature: ");
      Serial.print(myDHT22.getTemperatureC());
      Serial.print("C\t|\tHumidity: ");
      Serial.print(myDHT22.getHumidity());
      Serial.println("%");    
  }
  else
  {
    char buffer[50];
    myDHT22.getErrorString(errorCode, buffer);
    Serial.println(buffer);
  } 
}
