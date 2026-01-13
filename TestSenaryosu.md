# Yazılım Doğrulama ve Geçerleme - Test Senaryosu Raporu

**Proje Adı:** Banking System Test Automation
**Hazırlayan:** Tahir Tolu
**Tarih:** 13.01.2026

Bu rapor, Bankacılık Uygulaması (Banking System) üzerinde gerçekleştirilen otomatik test senaryolarını detaylandırmaktadır. Her bir senaryo, sistemin doğruluğunu ve güvenilirliğini test etmek amacıyla oluşturulmuştur.

---

## 1. Kullanıcı Kayıt Senaryosu (User Registration)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-001** (REQ-001) |
| **İlgili Gereksinimler** | Kullanıcı, geçerli kimlik ve iletişim bilgileri ile sisteme yeni kayıt oluşturabilmelidir. |
| **Ön Koşullar** | 1. Docker container'ları (backend, frontend, db) ayakta olmalıdır.<br>2. Kullanıcı daha önce aynı e-posta veya kullanıcı adı ile kayıt olmamış olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Tarayıcıda uygulama anasayfası (`http://localhost:8082`) açılır.<br>2. "Kayıt Ol" butonuna tıklanır.<br>3. Kayıt formu (Kullanıcı Adı, Şifre, E-posta, Ad, Soyad, Telefon) geçerli verilerle doldurulur.<br>4. "Kayıt Ol" butonuna basılır.<br>5. Sistem yanıtı beklenir. |
| **Beklenen Sonuç** | Ekranda "Kayıt Başarılı" mesajı görüntülenmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Veritabanında (Users tablosu) yeni kullanıcı kaydı oluşturulmuş olmalıdır. API üzerinden kullanıcı doğrulanabilmelidir (HTTP 200/403). |

---

## 2. Kullanıcı Giriş Senaryosu (User Login)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-002** (REQ-002) |
| **İlgili Gereksinimler** | Kayıtlı kullanıcı, doğru kullanıcı adı ve şifre ile sisteme giriş yapabilmelidir. |
| **Ön Koşullar** | 1. Kullanıcı sisteme kayıtlı olmalıdır.<br>2. Kullanıcı çıkış yapmış durumda olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Anasayfada "Giriş Yap" butonuna tıklanır.<br>2. "Kullanıcı Adı" ve "Şifre" alanları doğru bilgilerle doldurulur.<br>3. "Giriş Yap" butonuna basılır.<br>4. Yükleme ekranı beklenir. |
| **Beklenen Sonuç** | Giriş işlemi başarılı olmalı ve kullanıcı "Dashboard" (Panel) sayfasına yönlendirilmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Tarayıcıda geçerli bir JWT (Session Token) oluşturulmalı ve kullanıcı paneli aktif olmalıdır. |

---

## 3. Hesap Oluşturma Senaryosu (Account Creation)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-003** (REQ-003) |
| **İlgili Gereksinimler** | Giriş yapmış kullanıcı, Vadesiz (CHECKING) veya Vadeli (SAVINGS) hesap açabilmelidir. |
| **Ön Koşullar** | Kullanıcı sisteme giriş yapmış (Login) durumda olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Dashboard sayfasında "Hesap Oluştur" bölümüne gidilir.<br>2. Hesap Türü olarak "CHECKING" (Vadesiz) seçilir ve oluşturulur.<br>3. Hesap Türü olarak "SAVINGS" (Vadeli) seçilir ve oluşturulur. |
| **Beklenen Sonuç** | Her iki hesap türü için de "Hesap başarıyla oluşturuldu" mesajı alınmalı ve hesap listesinde yeni hesaplar görünmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Veritabanında (Accounts tablosu) ilgili kullanıcıya bağlı iki yeni hesap kaydı bulunmalıdır. |

---

## 4. Para Yatırma Senaryosu (Deposit)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-004** (REQ-004) |
| **İlgili Gereksinimler** | Kullanıcı mevcut hesabına para yatırabilmelidir. Bakiye güncellenmelidir. |
| **Ön Koşullar** | Kullanıcının en az bir aktif hesabı bulunmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. "Para Yatır" sekmesine tıklanır.<br>2. Yatırılacak hesap seçilir.<br>3. Tutar (örn: 1000.00 TL) girilir.<br>4. "Yatır" butonuna basılır. |
| **Beklenen Sonuç** | "Para yatırma işlemi başarılı" mesajı görülmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Seçilen hesabın bakiyesi, yatırılan tutar kadar artmalıdır (Eski Bakiye + 1000). |

---

## 5. Para Çekme Senaryosu (Withdrawal)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-005** (REQ-005) |
| **İlgili Gereksinimler** | Kullanıcı hesabından para çekebilmeli, bakiye düşmelidir. Yetersiz bakiye durumunda işlem yapılmamalıdır. |
| **Ön Koşullar** | Kullanıcının hesabında çekilmek istenen tutar kadar bakiye bulunmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. "Para Çek" sekmesine tıklanır.<br>2. Hesap seçilir.<br>3. Tutar (örn: 100.00 TL) girilir.<br>4. "Çek" butonuna basılır. |
| **Beklenen Sonuç** | "Para çekme işlemi başarılı" mesajı görülmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Seçilen hesabın bakiyesi, çekilen tutar kadar azalmalıdır (Eski Bakiye - 100). |

---

## 6. Para Transferi Senaryosu (Money Transfer)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-006** (REQ-006) |
| **İlgili Gereksinimler** | Kullanıcı kendi hesapları arasında veya başka bir hesaba para transferi yapabilmelidir. |
| **Ön Koşullar** | Kullanıcının en az iki hesabı olmalı ve gönderici hesapta yeterli bakiye bulunmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. "Transfer" sekmesine tıklanır.<br>2. Gönderen hesap (Vadesiz) seçilir.<br>3. Alıcı hesap (Vadeli) numarası girilir.<br>4. Tutar (örn: 50.00 TL) girilir.<br>5. "Transfer Et" butonuna basılır. |
| **Beklenen Sonuç** | "Transfer Başarılı" mesajı alınmalıdır. |
| **Son Koşullar / Beklenen Sistem Durumu** | Gönderen hesabın bakiyesi 50 TL azalmalı, alıcı hesabın bakiyesi 50 TL artmalıdır. |

---

## 7. Bakiye Sorgulama Senaryosu (Balance Inquiry)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-007** (REQ-007) |
| **İlgili Gereksinimler** | Kullanıcı hesaplarının güncel bakiyesini Dashboard üzerinde doğru şekilde görebilmelidir. |
| **Ön Koşullar** | Kullanıcının hesaplarında hareket (para yatırma/çekme) olmuş olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Dashboard (Hesaplarım) sayfası yenilenir.<br>2. İlgili hesabın "Bakiye" kolonu kontrol edilir. |
| **Beklenen Sonuç** | Ekranda görünen bakiye, veritabanındaki güncel bakiye ile birebir eşleşmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Sistem sadece okuma (Read) işlemi yapar, veri değişmemelidir. |

---

## 8. İşlem Geçmişi Senaryosu (Transaction History)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-008** (REQ-008) |
| **İlgili Gereksinimler** | Kullanıcı yaptığı tüm işlemleri (Yatırma, Çekme, Transfer) geçmiş listesinde görebilmelidir. |
| **Ön Koşullar** | Kullanıcı en az bir finansal işlem yapmış olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. "İşlem Geçmişi" sekmesine tıklanır.<br>2. Listelenen tabloda son yapılan işlem aranır. |
| **Beklenen Sonuç** | Son yapılan işlem (Tarih, Tutar, Açıklama) tabloda listelenmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Transactions tablosundaki kayıtlar eksiksiz listelenmelidir. |

---

## 9. Geçersiz Giriş Senaryosu (Negative Testing - Invalid Login)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-009** (REQ-009) |
| **İlgili Gereksinimler** | Sistem, hatalı kullanıcı adı veya şifre girişlerini engellemelidir. |
| **Ön Koşullar** | Kullanıcı giriş sayfasında olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Geçerli bir kullanıcı adı girilir.<br>2. **Yanlış** bir şifre girilir.<br>3. "Giriş Yap" butonuna basılır. |
| **Beklenen Sonuç** | Sisteme giriş yapılamamalı ve ekranda "Hatalı kullanıcı adı veya şifre" uyarısı çıkmalıdır. |
| **Son Koşullar / Beklenen Sistem Durumu** | Kullanıcı oturumu açılmamalıdır (JWT üretilmemelidir). |

---

## 10. Güvenli Çıkış Senaryosu (Logout)

| Başlık | Detay |
| :--- | :--- |
| **Test Durumu Kimliği** | **TS-010** (REQ-010) |
| **İlgili Gereksinimler** | Kullanıcı oturumunu güvenli bir şekilde sonlandırabilmelidir. |
| **Ön Koşullar** | Kullanıcı sisteme giriş yapmış olmalıdır. |
| **Adım Adım Uygulanacak İşlemler** | 1. Sağ üst köşedeki veya menüdeki "Çıkış Yap" (Logout) butonuna tıklanır. |
| **Beklenen Sonuç** | Kullanıcı anasayfaya yönlendirilmeli ve Dashboard erişimi kesilmelidir. |
| **Son Koşullar / Beklenen Sistem Durumu** | Tarayıcıdaki oturum anahtarı (Token) silinmeli, geri tuşuna basıldığında tekrar panele girilememelidir. |
