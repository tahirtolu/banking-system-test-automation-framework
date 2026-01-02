# Bankacılık Sistemi - YDG Final Projesi

Bu proje, Yazılım Doğrulama ve Geçerleme dersi için geliştirilmiş kapsamlı bir bankacılık sistemidir. Proje, CI/CD süreçleri ile test işlemlerini otomatikleştirmek için Jenkins kullanmaktadır.

## 📋 Proje Özellikleri

### Backend
- **Spring Boot 3.2.0** ile geliştirilmiş RESTful API
- **PostgreSQL** veritabanı
- **JWT** tabanlı kimlik doğrulama
- **Spring Security** ile güvenlik
- **JPA/Hibernate** ile veritabanı yönetimi

### Frontend
- Modern ve kullanıcı dostu HTML/CSS/JavaScript arayüzü
- Selenium testleri için optimize edilmiş yapı

### Testler
- **Birim Testleri (Unit Tests)**: Service katmanı testleri
- **Entegrasyon Testleri (Integration Tests)**: Controller katmanı testleri
- **Sistem Testleri (Selenium)**: 10 adet end-to-end test senaryosu

### CI/CD
- **Jenkins** pipeline ile otomatik test ve deployment
- **Docker** container'lar ile izole ortam
- **Docker Compose** ile çoklu servis yönetimi

## 🏗️ Proje Yapısı

```
odev/
├── src/
│   ├── main/
│   │   ├── java/com/banking/
│   │   │   ├── entity/          # Veritabanı entity'leri
│   │   │   ├── repository/      # JPA repository'ler
│   │   │   ├── service/         # İş mantığı servisleri
│   │   │   ├── controller/     # REST controller'lar
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── config/          # Yapılandırma sınıfları
│   │   │   ├── filter/          # JWT filter
│   │   │   └── util/            # Yardımcı sınıflar
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/banking/
│       │   ├── service/          # Birim testleri
│       │   └── integration/     # Entegrasyon testleri
├── frontend/                     # Frontend uygulaması
├── selenium-tests/               # Selenium test senaryoları
├── Dockerfile                    # Docker image tanımı
├── docker-compose.yml            # Docker Compose yapılandırması
├── Jenkinsfile                   # Jenkins CI/CD pipeline
└── pom.xml                       # Maven yapılandırması
```

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler
- Java 17
- Maven 3.9+
- Docker ve Docker Compose
- PostgreSQL 15+ (veya Docker ile)
- Jenkins (CI/CD için)

### Yerel Geliştirme Ortamı

1. **Veritabanını başlatın:**
```bash
docker-compose up -d postgres
```

2. **Uygulamayı çalıştırın:**
```bash
mvn spring-boot:run
```

3. **Frontend'i açın:**
```bash
# frontend/index.html dosyasını tarayıcıda açın
```

### Docker ile Çalıştırma

```bash
# Tüm servisleri başlat
docker-compose up -d

# Logları görüntüle
docker-compose logs -f banking-app

# Servisleri durdur
docker-compose down
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Kullanıcı kaydı
- `POST /api/auth/login` - Kullanıcı girişi

### Accounts
- `POST /api/accounts?accountType={CHECKING|SAVINGS}` - Hesap oluştur
- `GET /api/accounts` - Kullanıcı hesaplarını listele
- `GET /api/accounts/{accountNumber}` - Hesap detayları

### Transactions
- `POST /api/transactions/{accountNumber}/deposit` - Para yatır
- `POST /api/transactions/{accountNumber}/withdraw` - Para çek
- `POST /api/transactions/{accountNumber}/transfer` - Para transferi
- `GET /api/transactions/{accountNumber}/history` - İşlem geçmişi

## 🧪 Test Senaryoları

### Birim Testleri
- `UserServiceTest` - Kullanıcı servisi testleri
- `AccountServiceTest` - Hesap servisi testleri
- `TransactionServiceTest` - İşlem servisi testleri

### Entegrasyon Testleri
- `AuthControllerIntegrationTest` - Kimlik doğrulama testleri
- `AccountControllerIntegrationTest` - Hesap yönetimi testleri
- `TransactionControllerIntegrationTest` - İşlem testleri

### Selenium Test Senaryoları (10 Adet)

1. **Test1_UserRegistration** - Kullanıcı kaydı
2. **Test2_UserLogin** - Kullanıcı girişi
3. **Test3_AccountCreation** - Hesap oluşturma
4. **Test4_Deposit** - Para yatırma
5. **Test5_Withdrawal** - Para çekme
6. **Test6_Transfer** - Para transferi
7. **Test7_BalanceInquiry** - Bakiye sorgulama
8. **Test8_TransactionHistory** - İşlem geçmişi görüntüleme
9. **Test9_InvalidLogin** - Geçersiz giriş denemesi
10. **Test10_Logout** - Çıkış yapma

## 🔄 CI/CD Pipeline

Jenkins pipeline aşağıdaki aşamaları içerir:

1. **Checkout** (5 puan) - GitHub'dan kod çekme
2. **Build** (5 puan) - Kod derleme
3. **Unit Tests** (15 puan) - Birim testleri çalıştırma ve raporlama
4. **Integration Tests** (15 puan) - Entegrasyon testleri çalıştırma ve raporlama
5. **Docker Build** - Docker image oluşturma
6. **Start Containers** (5 puan) - Docker container'ları başlatma
7. **Health Check** - Sistem sağlık kontrolü
8. **Selenium Tests** (55 puan + ek puanlar) - 10 adet sistem testi

### Jenkins Kurulumu

1. Jenkins'i kurun ve başlatın
2. Gerekli plugin'leri yükleyin:
   - Pipeline
   - JUnit
   - Docker Pipeline
3. Maven ve JDK tool'larını yapılandırın
4. Pipeline'ı oluşturun ve Jenkinsfile'ı kullanın

## 📊 Use Case'ler

### 1. Kullanıcı Kaydı
- Kullanıcı sisteme kayıt olur
- Kullanıcı adı, şifre, e-posta, ad, soyad ve telefon bilgileri alınır

### 2. Oturum Açma
- **Ön koşul**: Kullanıcı kaydının tamamlanması
- Kullanıcı adı ve şifre ile giriş yapılır
- JWT token döndürülür

### 3. Hesap Açma
- **Ön koşul**: Oturum açma işleminin tamamlanması
- Vadesiz veya Vadeli hesap açılabilir
- Her hesaba benzersiz hesap numarası atanır

### 4. Para Yatırma
- **Ön koşul**: Hesap açma işleminin tamamlanması
- Hesaba para yatırılır
- İşlem kaydı oluşturulur

### 5. Para Çekme
- **Ön koşul**: Hesapta yeterli bakiye olması
- Hesaptan para çekilir
- İşlem kaydı oluşturulur

### 6. Para Transferi
- **Ön koşul**: İki hesabın olması ve gönderen hesapta yeterli bakiye
- Bir hesaptan diğerine para transferi yapılır
- İşlem kaydı oluşturulur

### 7. Bakiye Sorgulama
- Kullanıcının tüm hesapları ve bakiyeleri görüntülenir

### 8. İşlem Geçmişi
- Seçilen hesabın tüm işlem geçmişi görüntülenir

## 🔒 Güvenlik

- JWT tabanlı kimlik doğrulama
- BCrypt ile şifre hashleme
- CORS yapılandırması
- Spring Security ile endpoint koruması

## 📦 Bağımlılıklar

Ana bağımlılıklar:
- Spring Boot 3.2.0
- PostgreSQL Driver
- JWT (jjwt 0.12.3)
- Selenium 4.15.0
- WebDriverManager 5.6.2
- JUnit 5

## 🐛 Sorun Giderme

### Veritabanı Bağlantı Hatası
- PostgreSQL'in çalıştığından emin olun
- `application.properties` dosyasındaki veritabanı bilgilerini kontrol edin

### Docker Sorunları
- Docker ve Docker Compose'un yüklü olduğundan emin olun
- Port çakışmalarını kontrol edin (8080, 5432)

### Test Hataları
- Selenium testleri için ChromeDriver'ın yüklü olduğundan emin olun
- Frontend dosyalarının doğru konumda olduğunu kontrol edin



## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.

