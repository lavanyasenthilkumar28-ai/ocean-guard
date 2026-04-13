#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <SoftwareSerial.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

SoftwareSerial gpsSerial(D6, D5);

// LCD
LiquidCrystal_I2C lcd(0x27,16,2);

// 🔵 WiFi
#define WIFI_SSID "jiya"
#define WIFI_PASSWORD "lavu123j"

// 🔵 Firebase Live Path
#define FIREBASE_URL "https://aquawatchdata-default-rtdb.firebaseio.com/wearable_001/live.json"

// 🔴 Output Pins
#define BUZZER D2
#define LED D1

SoftwareSerial bt(D7,D8);

void setup() {

  Serial.begin(9600);
  gpsSerial.begin(9600);
  bt.begin(9600);

  pinMode(BUZZER, OUTPUT);
  pinMode(LED, OUTPUT);

  digitalWrite(BUZZER, LOW);
  digitalWrite(LED, LOW);

  // LCD INIT
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0,0);
  lcd.print("System Ready");
  delay(2000);
  lcd.clear();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting WiFi");

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi Connected");
}

void loop() {

  if(bt.available())
  {
    String data= bt.readStringUntil('\n');
    data.trim();
    Serial.println(data);

    if(data == "SEVERE FALL")
    {
      digitalWrite(LED,HIGH);
      digitalWrite(BUZZER,HIGH);

      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("MAN OVERBOARD");

      delay(5000);
    }
    else if(data == "NORMAL")
    {
      digitalWrite(LED,LOW);
      digitalWrite(BUZZER,LOW);

      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("NO EMERGENCY");
    }
  }
}