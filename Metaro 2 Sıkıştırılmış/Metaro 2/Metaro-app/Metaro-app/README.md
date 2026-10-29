# Metaro App ✈️ 🌦️

*(Work in Progress / Aktif Geliştirme Aşamasında)*

**[English](https://www.google.com/search?q=%23english)** | **[Türkçe](https://www.google.com/search?q=%23t%C3%BCrk%C3%A7e)**

---

## 🇬🇧 English

### About the Project

**Metaro App** is a modern Android application built with **Jetpack Compose**, designed for pilots, aviation enthusiasts, and flight planning. The project provides real-time aviation weather reports, decodes complex METAR and TAF data, tracks UTC/Zulu time across global aviation hubs, and offers interactive flight route map previews.

This project was initially kicked off during the **Kamp+ Bilişim Kampı** (Kamp+ IT Bootcamp) and is **currently under active development (WIP)** with continuous UI/UX refinements, new features, and architectural enhancements.

### Key Features

* 📡 **METAR & TAF Decoding:** Fetches live weather data via official NOAA / Aviation Weather Center endpoints and decodes wind, visibility, cloud layers, temperature, dew point, and QNH altimeter settings.
* 🚦 **Dynamic Flight Categories:** Automatically categorizes weather conditions using color-coded badges for **VFR** (Visual Flight Rules), **MVFR**, **IFR**, and **LIFR**.
* ⏱️ **Zulu Clock & Global UTC Dashboard:** Displays live UTC time along with active aviation offsets across key hubs (LON, IST, JFK, HND) and includes a simulated flight progress tracker.
* 🗺️ **Interactive Tactical Map:** Powered by Leaflet.js and CartoDB Dark tiles, allowing pilots to enter departure/arrival ICAOs, plot route curves, and save brief flight notes.
* 💬 **Friendly Weather Advisory Engine:** Context-aware engine that analyzes destination temperature, wind, and precipitation to offer concise equipment and clothing tips.
* ⚠️ **Flight Safety Disclaimer:** Explicitly built as an educational and quick-preview tool—not for actual "GO / NO-GO" operational decision-making (official meteorology and CFI approval remain mandatory).

### Tech Stack & Libraries

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material3)
* **Architecture:** MVVM, Clean Architecture concepts
* **Navigation:** Jetpack Compose Navigation (`NavHost`)
* **Web View Enclosure:** AndroidView + Leaflet.js (CartoDB Dark Tiles)
* **Networking & Async:** Kotlin Coroutines, Retrofit / OkHttp, Open-Meteo & NOAA APIs

---

## 🇹🇷 Türkçe

### Proje Hakkında

**Metaro App**, pilotlar, havacılık meraklıları ve uçuş planlaması yapanlar için **Jetpack Compose** ile geliştirilmiş modern bir Android uygulamasıdır. Uygulama; anlık havacılık hava durumu raporlarını sunar, karmaşık METAR ve TAF verilerini çözümler, küresel havacılık merkezlerindeki UTC/Zulu saatlerini takip eder ve etkileşimli uçuş rotası önizlemeleri sağlar.

Bu proje, **Turkcell & GSB Kamp+ Bilişim Kampı** bünyesinde geliştirilmeye başlanmış olup **aktif olarak geliştirilmeye (WIP)** ve yeni arayüz/fonksiyonel özelliklerle güncellenmeye devam etmektedir.

### Öne Çıkan Özellikler

* 📡 **METAR & TAF Dekodörü:** Resmî NOAA / Aviation Weather Center kaynaklarından canlı veri çekerek rüzgâr, görüş mesafesi, bulut tabakası, sıcaklık, çiğ noktası ve QNH altimetre basınç değerlerini anlaşılır Türkçe/İngilizce metinlere dönüştürür.
* 🚦 **Dinamik Uçuş Kategorileri:** Hava koşullarını tavan ve görüş mesafesine göre renk kodlu rozetlerle **VFR** (Görerek Uçuş), **MVFR**, **IFR** ve **LIFR** olarak kategorize eder.
* ⏱️ **Zulu Saati ve Küresel UTC Paneli:** Canlı akan UTC (Zulu) saatinin yanı sıra ana havacılık merkezlerinin (LON, IST, JFK, HND) yerel zamanlarını ve kavisli rota üzeri canlı uçak takibini sunar.
* 🗺️ **İnteraktif Taktik Harita:** Leaflet.js ve CartoDB Dark teması altyapısıyla kalkış/varış ICAO kodlarına göre rota çizer, harita üzerinde gezinti imkânı sunar ve kısa uçuş notları kaydeder.
* 💬 **Samimi Uçuş Dipnot Motoru:** Varış noktasındaki canlı sıcaklık, rüzgâr ve yağış değerlerini analiz ederek pilota kıyafet ve ekipman tavsiyelerinde bulunur.
* ⚠️ **Uçuş Güvenliği Uyarısı:** Uygulama resmî "GO / NO-GO" uçuş kararı vermez; eğitim ve hızlı ön izleme amacıyla tasarlanmıştır (resmî kaynaklar ve eğitmen onayı esastır).

### Teknolojik Altyapı

* **Dil:** Kotlin
* **Arayüz (UI):** Jetpack Compose (Material3)
* **Mimari:** MVVM, Clean Architecture ilkeleri
* **Navigasyon:** Jetpack Compose Navigation (`NavHost`)
* **Harita Entegrasyonu:** AndroidView + Leaflet.js (CartoDB Dark Tema)
* **Ağ & Asenkron:** Kotlin Coroutines, Retrofit / OkHttp, Open-Meteo & NOAA API

---

### Disclaimer / Yasal Sorumluluk Reddi

> **EN:** This software is developed solely for educational, testing, and flight preparation preview purposes. It must not be used as the sole primary source for operational flight decisions (GO / NO-GO). Always consult official aviation meteorology sources (e.g., AviationWeather.gov, local MET offices) and obtain instructor (CFI) authorization prior to flight.
> **TR:** Bu yazılım yalnızca eğitim, test ve uçuş hazırlığı ön izleme amacıyla geliştirilmiştir. Operasyonel uçuş kararlarında (GO / NO-GO) tek ana kaynak olarak kullanılamaz. Uçuş öncesinde resmî havacılık meteorolojisi kaynaklarına (AviationWeather.gov, MGM vb.) başvurulması ve yetkili uçuş eğitmeni (CFI) onayı alınması zorunludur.
