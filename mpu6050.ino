#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClientSecure.h>

#include <Wire.h>
#include <MPU6050.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <time.h>

SoftwareSerial bt(D7,D8);

// ---------------- WIFI ----------------
#define WIFI_SSID "jiya"
#define WIFI_PASSWORD "lavu123j"

// ---------------- FIREBASE (HTTPS ONLY) ----------------
#define FIREBASE_LIVE_URL "https://aquawatchdata-default-rtdb.firebaseio.com/wearable_001/live.json"
#define FIREBASE_LOG_URL  "https://aquawatchdata-default-rtdb.firebaseio.com/wearable_001/logs.json"

// ---------------- SENSORS ----------------
#define WATER_SENSOR A0
#define WATER_THRESHOLD 300

MPU6050 mpu;
TinyGPSPlus gps;
SoftwareSerial gpsSerial(D6, D5);

// ---------------- VARIABLES ----------------
int16_t ax, ay, az;
long magnitude = 0;
int waterValue = 0;

static unsigned long confirmStart = 0;
static unsigned long fallStart = 0;
static int waterCount = 0;

bool waterConfirmed = false;
bool fallDetected = false;
bool manOverboard = false;

String fallLevel = "NORMAL";

double latitude = 10.814713;
double longitude = 78.67307;

// =======================================================

void setup() {

  Serial.begin(9600);
  bt.begin(9600);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected");

  // 🔥 FIX TIME (1970 / 1917 PROBLEM)
  configTime(0, 0, "pool.ntp.org");

  Wire.begin(D2, D1);
  mpu.initialize();
  Serial.println(mpu.testConnection() ? "MPU6050 Connected" : "MPU6050 NOT Connected");

  pinMode(WATER_SENSOR, INPUT);

  gpsSerial.begin(9600);
  Serial.println("GPS Started");
}

// =======================================================

void loop() {

  // ---------------- GPS ----------------
  while (gpsSerial.available()) {
    gps.encode(gpsSerial.read());
  }

  if (gps.location.isValid()) {
    latitude = gps.location.lat();
    longitude = gps.location.lng();
  }

  String gpsStatus = gps.location.isValid() ? "FIXED" : "SEARCHING";

  String mapLink = "https://www.google.com/maps?q=" +
                   String(latitude, 6) + "," +
                   String(longitude, 6);

  // ---------------- WATER ----------------
  waterValue = analogRead(WATER_SENSOR);
  if (waterValue > WATER_THRESHOLD) waterCount++;
  else waterCount = 0;

  waterConfirmed = (waterCount >= 2);
  Serial.print("Water Raw: ");
Serial.println(waterValue);

  // ---------------- FALL ----------------
  mpu.getAcceleration(&ax, &ay, &az);
  long axl = ax;
  long ayl = ay;
  long azl = az;

  magnitude = sqrt(axl*axl + ayl*ayl + azl*azl);

  fallDetected = (magnitude > 20000);

  if (magnitude > 60000) fallLevel = "SEVERE FALL";
  else if (magnitude > 40000) fallLevel = "MINOR FALL";
  else fallLevel = "NORMAL";

if(fallLevel == "SEVERE FALL")
{
  bt.println("SEVERE FALL");
}
else
{
bt.println("NORMAL");
}

  bool dangerCandidate = (waterConfirmed && fallDetected);

  if (dangerCandidate) {
    if (confirmStart == 0) confirmStart = millis();
  } else {
    confirmStart = 0;
    manOverboard = false;
  }

  if (confirmStart > 0 && millis() - confirmStart > 1500) {
    manOverboard = true;
  }

  if (manOverboard && fallStart == 0) fallStart = millis();

  if (fallStart > 0 && magnitude < 20000 && waterValue < WATER_THRESHOLD) {
    if (millis() - fallStart > 4000) {
      manOverboard = false;
      fallStart = 0;
      confirmStart = 0;
    }
  }

  String waterStatus = waterConfirmed ? "WATER CONFIRMED" : "NO WATER";
  String finalStatus = manOverboard ? "MAN OVERBOARD" : "SAFE";

  Serial.println("------------------------------");
  Serial.print("STATUS: ");
  Serial.println(finalStatus);

  // ================= FIREBASE =================
  if (WiFi.status() == WL_CONNECTED) {

    WiFiClientSecure client;
    client.setInsecure();          // 🔥 REQUIRED
    client.setTimeout(15000);

    unsigned long nowTime = time(nullptr);   // 🔥 REAL TIME

    String json =
      "{"
        "\"gps\":{"
          "\"lat\":" + String(latitude, 6) + ","
          "\"lng\":" + String(longitude, 6) + ","
          "\"status\":\"" + gpsStatus + "\","
          "\"map\":\"" + mapLink + "\""
        "},"
        "\"water\":{"
          "\"value\":" + String(waterValue) + ","
          "\"status\":\"" + waterStatus + "\""
        "},"
        "\"fall\":{"
          "\"magnitude\":" + String(magnitude) + ","
          "\"level\":\"" + fallLevel + "\""
        "},"
        "\"alert\":\"" + finalStatus + "\","
        "\"timestamp\":" + String(nowTime) +
      "}";

    // ---------- LIVE ----------
    HTTPClient httpLive;
    httpLive.begin(client, FIREBASE_LIVE_URL);
    httpLive.addHeader("Content-Type", "application/json");
    int liveCode = httpLive.PUT(json);
    Serial.print("LIVE CODE: ");
    Serial.println(liveCode);
    httpLive.end();

    // ---------- LOG ----------
    HTTPClient httpLog;
    httpLog.begin(client, FIREBASE_LOG_URL);
    httpLog.addHeader("Content-Type", "application/json");
    int logCode = httpLog.POST(json);
    Serial.print("LOG CODE: ");
    Serial.println(logCode);
    httpLog.end();
  }

  delay(1000);
}