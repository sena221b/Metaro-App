🚀 Metaro App

    Gözümüz Yükseklerde, Hava Durumu Cebinizde! ✈️🌤️

Bu proje Kamp+ GSB / Turkcell Mobil Uygulama Geliştirme kampı atölye çıktısı olarak hazırlanmıştır.
📌 Proje Hakkında

Metaro App, havacılık tutkunları, pilotlar ve meteoroloji meraklıları için geliştirilmiş modern bir Android havacılık hava durumu ve bilgilendirme uygulamasıdır. Havalimanlarına ait METAR ve TAF kodlarını çözümleyerek anlaşılır görsel formatlara dönüştürür, uçuş emniyetine yönelik kritik hava durumu verilerini sunar.
✨ Öne Çıkan Özellikler

    📡 METAR & TAF Dekoderi: Karmaşık havacılık hava durumu kodlarını anlaşılır ve görsel formatlara dönüştürme.

    🕒 Zulu / UTC Zaman Saati: Havacılık standartlarına uygun eşzamanlı UTC zaman gösterimi.

    🗺️ Etkileşimli Rota & Harita: Havalimanı lokasyonları ve hava şartlarının harita üzerinde görselleştirilmesi.

    ⚠️ Uçuş Emniyeti Uyarıları (Advisories): Kritik hava olayları (türbülans, buzlanma, düşük görüş) için anlık uyarı ve bilgilendirmeler.

🛠️ Kullanılan Teknolojiler & Kütüphaneler

    Dil: Kotlin

    Arayüz (UI): Jetpack Compose & Material 3

    Ağ / API: Retrofit & OkHttp

    Harita Entegrasyonu: Leaflet.js / WebView & Interactive Maps

    Eşzamanlılık (Concurrency): Kotlin Coroutines & Flow

    Derleme Sistemi: Gradle (Kotlin DSL / Groovy)

📁 Proje Yapısı
Plaintext

Metaro-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/.../metaro/   # Kotlin Kaynak Kodları (UI, Data, Domain)
│   │   │   ├── res/                   # Görsel, Renk ve Arayüz Kaynakları
│   │   │   └── AndroidManifest.xml    # Uygulama Yapılandırma Dosyası
│   └── build.gradle.kts               # Modül Seviyesi Gradle Yapılandırması
├── build.gradle.kts                   # Proje Seviyesi Gradle Yapılandırması
└── README.md                          # Proje Dokümantasyonu

🚀 Kurulum ve Çalıştırma

Projeyi kendi yerel ortamınızda çalıştırmak için aşağıdaki adımları izleyebilirsiniz:

    Repoyu klonlayın:
    Bash

    git clone https://github.com/sena221b/Metaro-app.git

    Android Studio'da Açın:

        Android Studio (Ladybug veya daha yeni bir sürüm) çalıştırın.

        Open an Existing Project seçeneği ile Metaro-app klasörünü seçin.

    Gradle Senkronizasyonu:

        Projenin bağımlılıklarının yüklenmesi için Gradle senkronizasyonunun tamamlanmasını bekleyin.

    Çalıştırın:

        Bir emülatör veya fiziksel Android cihaz bağlayarak Run App (Shift + F10) butonuna basın.
