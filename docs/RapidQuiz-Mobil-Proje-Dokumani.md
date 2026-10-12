# Rapid Quiz Mobil (Android) — Proje Dokümanı

> Sürüm 1.0 · 12 Ekim 2026 · Kotlin + Jetpack Compose
> Kaynak: canlı backend (`RapidQuizBackend`), web istemcisi (`RapidQuizFrontend`) ve
> `src/api/openapi.yaml`. Buradaki tüm uç noktalar, alan adları ve hata kodları bu
> kaynaklardan doğrulanmıştır.

---

## 1. Amaç ve kapsam

Web'deki Rapid Quiz'in Android'e **olduğu gibi** taşınması. Yeni özellik yok.

**Kullanıcı akışı**

1. Uygulama açılır → 5 kategori kartı görünür.
2. Kategoriye dokunulur → 20 soru, soru başına 5 saniye, 4 şık.
3. Quiz bitince sonuç ekranı → isim girilir → skor tabloya kaydedilir.
4. Skor Tablosu: kategori sekmeleri + seçili kategorinin top 10'u (webdeki ile aynı).

**Kapsam dışı (bilerek):** kayıt/giriş, profil, ayarlar, bildirim, çevrimdışı mod, iOS,
tablet düzeni, çoklu dil (yalnızca Türkçe), koyu tema (web'de de yok).

**Ekran sayısı: 4** — Ana Sayfa, Quiz, Sonuç, Skor Tablosu.

---

## 2. Backend'de değişiklik gerekli mi?

**Hayır — mobil uygulamaya başlamak için zorunlu bir backend değişikliği yok.**
Kontrol edilenler:

| Konu | Durum |
|---|---|
| `platform` alanı | `SessionCreate` zaten `web`/`ios`/`android` kabul ediyor (varsayılan `web`). Mobil `"android"` gönderecek. |
| CORS / CSRF | Yalnızca tarayıcıyı ilgilendirir; yerel (native) HTTP istemcileri etkilenmez. `CORS_ALLOWED_ORIGINS`'e dokunulmaz. |
| Kimlik doğrulama | Cookie yok; `X-Session-Token` başlığı yeterli (web ile aynı). |
| Hız sınırı | `POST /sessions/` IP başına saatte 60, diğerleri dakikada 120. Normal oyun için yeterli (bir oyun ≈ 45 istek). |
| Skor tablosu | `GET /leaderboard/?category=...` kategori bazlı, mobilin ihtiyacını karşılıyor. |

**Bilmen gereken ama değişiklik gerektirmeyen noktalar**

1. **"Genel scoreboard" yorumu.** Web'de "genel" bir tablo yok; skor tablosu *kategori sekmeli*
   (kategori başına top 10). Mobilde de aynısını yapacağız. Eğer tüm kategorileri birleştiren
   tek bir liste istiyorsan **o zaman backend'e yeni uç gerekir** (`/leaderboard/` şu an
   `category` parametresi olmadan 400 döner). Bkz. Bölüm 13 – Açık soru 1.
2. **OpenAPI sapması.** Şema `POST /sessions/` ve `POST /score/` için `200` yazıyor, backend
   gerçekte **`201`** dönüyor. Mobil istemci "2xx = başarı" kabul etmeli (Retrofit `isSuccessful`
   zaten böyle).
3. **Render ücretsiz katmanı uykuya geçer** (15 dk boşta kalınca, ilk istek ~1 dk). Mobilde
   zaman aşımı ve "sunucu uyanıyor" durumu şart (Bölüm 9). Backend tarafı değişmez.
4. **Hız sınırı mobil ağlarda.** Operatör NAT'ı arkasında birçok kullanıcı tek IP paylaşabilir;
   60 oturum/saat sınırı bu durumda dolabilir. Şu an kullanıcı sayısı küçük olduğu için riski
   kabul ediyoruz; büyürse sınırın artırılması ya da cihaz bazlı anahtarlanması gerekir.
5. **Admin (`/admin/`) herkese açık** (mevcut bilinen durum, mobille ilgisi yok).

> İleride API değişirse web ile aynı akış: backend'de `spectacular --file schema.yaml` →
> `openapi.yaml` güncelle → mobil DTO'larını elle uyarla (Bölüm 6).

---

## 3. Teknoloji yığını

| Alan | Seçim |
|---|---|
| Dil / UI | Kotlin, Jetpack Compose, Material 3 |
| Mimari | Tek modül, MVVM: `ViewModel` + `StateFlow` + `UiState` (sealed) |
| Navigasyon | Navigation Compose (4 rota) |
| Ağ | Retrofit 2 + OkHttp 4 + `kotlinx.serialization` converter |
| Asenkron | Kotlin Coroutines / Flow |
| DI | Elle (basit bir `AppContainer`); Hilt kullanılmaz — 4 ekran için gereksiz |
| Test | JUnit5/JUnit4 + `kotlinx-coroutines-test` + MockWebServer; Compose UI testi (temel akış) |
| Build | Gradle Kotlin DSL + Version Catalog (`libs.versions.toml`), sürümler proje açılırken güncel kararlıya sabitlenir |
| minSdk / target | minSdk 26 (Android 8), targetSdk güncel kararlı |
| Yön | Yalnızca dikey (portrait) kilitli |

---

## 4. Altın kurallar (web ile aynı, mobile uyarlanmış)

1. **Token yalnızca bellekte.** `SharedPreferences`, DataStore, Room, dosya — hiçbirine yazılmaz.
   Uygulama kapanırsa/süreç ölürse oturum biter (web'de sayfa yenilemenin karşılığı).
2. **Doğru cevap istemcide hesaplanmaz/saklanmaz.** `is_correct` ve `correct_choice_id`
   yalnızca `POST .../answers/` yanıtından gelir.
3. **Süre sunucuda ölçülür.** Ekrandaki geri sayım yalnızca görseldir; `remaining_ms` ile
   başlar, kendi başına "süre doldu" kararı vermez — tek yaptığı `choice_id = null` göndermektir.
4. **Oturum kimliği `X-Session-Token` başlığıyla taşınır.** Cookie yok.
5. **Yalnızca HTTPS.** `usesCleartextTraffic="false"`; geliştirmede emülatör için
   `http://10.0.2.2:8000` yalnızca `debug` build'ine özel network security config ile açılır.
6. **Log'a token yazılmaz.** OkHttp logging interceptor yalnızca debug'da ve
   `X-Session-Token` başlığı `redactHeader` ile maskelenmiş olarak.

---

## 5. API — nereye istek atılır

### 5.1 Adresler

| | Adres |
|---|---|
| **Canlı base URL** | `https://rapidquizbackend.onrender.com/api/v1/` |
| Yerel geliştirme (emülatör) | `http://10.0.2.2:8000/api/v1/` (host makinedeki `docker compose up`) |
| Swagger (referans) | `https://rapidquizbackend.onrender.com/api/docs/` |
| Şema | `https://rapidquizbackend.onrender.com/api/schema/` |

Base URL `BuildConfig.API_BASE_URL` olarak `debug`/`release` için ayrı tanımlanır.
Retrofit base URL **`/` ile bitmeli**; yollar baştaki `/` olmadan yazılır.

### 5.2 Ortak kurallar

- `Content-Type: application/json` (yalnızca JSON; backend başka parser kabul etmez).
- Oturum gerektiren 4 uçta başlık: `X-Session-Token: <session_token>`.
- Tüm hatalar şu biçimde döner (Bölüm 5.4):
  ```json
  { "error": { "code": "question_mismatch", "message": "Gönderilen soru, ..." } }
  ```
- Zaman aşımı: `connect 20 sn`, `read/write 60 sn` (Render uyanışı için; web 45 sn kullanıyor,
  mobilde ağ daha değişken olduğundan biraz geniş).

### 5.3 Uç noktalar

| # | Metot | Yol | Başlık | Kullanıldığı ekran |
|---|---|---|---|---|
| 1 | GET | `categories/` | — | Ana Sayfa, Skor Tablosu sekmeleri |
| 2 | POST | `sessions/` | — | Quiz başlangıcı |
| 3 | GET | `sessions/{session_id}/question/` | token | Quiz (her soru) |
| 4 | POST | `sessions/{session_id}/answers/` | token | Quiz (her cevap/süre dolumu) |
| 5 | GET | `sessions/{session_id}/result/` | token | Sonuç |
| 6 | POST | `sessions/{session_id}/score/` | token | Sonuç (isim kaydet) |
| 7 | GET | `leaderboard/?category={slug}&limit=10` | — | Skor Tablosu |
| 8 | GET | `health/` | — | Açılışta sunucuyu uyandırma (isteğe bağlı) |

#### 1) `GET categories/` → `200`

```json
[
  { "name": "Yazılım", "slug": "yazilim", "description": "Programlama, web ve yazılım geliştirme kültürü",
    "icon": "code", "color": "#3B82F6", "question_count": 20 }
]
```
Seed'deki 5 kategori: `yazilim` (#3B82F6, `code`), `yapay-zeka` (#A855F7, `brain`),
`bilgisayar-muhendisligi` (#14B8A6, `cpu`), `ulkeler` (#F97316, `globe`), `fizik` (#EC4899, `atom`).

#### 2) `POST sessions/` → `201`

```json
// istek
{ "category": "yazilim", "platform": "android" }
// yanıt
{ "session_id": "3f0c…-uuid", "session_token": "…", 
  "category": { "slug": "yazilim", "name": "Yazılım" },
  "total_questions": 20, "time_limit_seconds": 5 }
```

#### 3) `GET sessions/{id}/question/` → `200`

**Bu çağrı o sorunun sayacını sunucuda başlatır.** İlk çağrıda `remaining_ms = time_limit_ms`;
aynı soru için tekrar çağrılırsa kalan süre azalarak döner.

```json
{ "index": 0, "total": 20,
  "question": { "id": 101, "text": "…" },
  "choices": [ { "id": 1011, "text": "…" }, { "id": 1012, "text": "…" },
               { "id": 1013, "text": "…" }, { "id": 1014, "text": "…" } ],
  "time_limit_ms": 5000, "remaining_ms": 5000 }
```
Şıklar sunucuda karıştırılmış sırada gelir — olduğu gibi gösterilir (A/B/C/D etiketi UI'da).

#### 4) `POST sessions/{id}/answers/` → `200`

```json
// istek (şık seçildi)            // istek (süre doldu)
{ "question_id": 101, "choice_id": 1012 }    { "question_id": 101, "choice_id": null }
// yanıt
{ "is_correct": true, "correct_choice_id": 1012, "points": 180,
  "response_ms": 1000, "score": 180, "finished": false }
```
`finished: true` → son soru cevaplandı; sonraki `question/` çağrısı yapılmaz, Sonuç'a geçilir.

#### 5) `GET sessions/{id}/result/` → `200` (yalnızca bitmiş oturum)

```json
{ "score": 2750, "correct_count": 17, "total_questions": 20, "total_time_ms": 41230,
  "category": { "slug": "yazilim", "name": "Yazılım" } }
```
Bitmemiş oturumda `409 session_not_finished`.

#### 6) `POST sessions/{id}/score/` → `201`

```json
// istek
{ "player_name": "Sude" }
// yanıt
{ "rank": 3, "in_top10": true,
  "leaderboard": [ { "rank": 1, "player_name": "…", "score": 3100, "correct_count": 19 } ] }
```
Oturum başına **yalnızca bir kez** (ikincisi `409 score_already_submitted`).

#### 7) `GET leaderboard/?category=yazilim&limit=10` → `200`

`category` **zorunlu** (yoksa 400). `limit` 1–50, varsayılan 10.
```json
[ { "rank": 1, "player_name": "…", "score": 3100, "correct_count": 19 } ]
```
Sıralama: yüksek puan → kısa toplam süre → daha erken kayıt (sunucu belirler; istemci sıralamaz).

### 5.4 Hata kodları ve mobilin tepkisi

| HTTP | `error.code` | Anlamı | İstemci tepkisi |
|---|---|---|---|
| 400 | `invalid_request` | Geçersiz gövde (örn. isim kuralı) | `message`'ı forma göster |
| 401 | `invalid_token` | Token eksik/hatalı | Oyunu bitir, Ana Sayfa'ya dön |
| 404 | `category_not_found` | Kategori yok | "Kategori bulunamadı", geri dön |
| 404 | `session_not_found` | Oturum yok | Ana Sayfa'ya dön |
| 409 | `question_mismatch` | Mevcut soruyla eşleşmiyor | `question/` ile **yeniden senkronize ol** |
| 409 | `already_answered` | Soru zaten cevaplanmış | `question/` ile yeniden senkronize ol |
| 409 | `session_already_completed` | Oturum bitmiş | `result/` ekranına geç |
| 409 | `session_not_finished` | Henüz bitmedi | `question/` ile devam et |
| 409 | `score_already_submitted` | Skor zaten kaydedilmiş | Skor Tablosu'na geç |
| 410 | `session_expired` | 180 sn işlem yok → oturum öldü | "Oturum süresi doldu", Ana Sayfa |
| 429 | `rate_limited` | İstek sınırı | "Çok hızlı, biraz bekle", tekrar dene |
| 5xx / ağ | — | Sunucu/ağ | "Tekrar Dene" (Bölüm 9) |

Hata gövdesi her zaman ayrıştırılamayabilir (örn. proxy 502 HTML döner): `error` alanı
yoksa genel bir mesaj göster.

---

## 6. Veri modelleri (Kotlin DTO taslağı)

```kotlin
@Serializable data class Category(
    val name: String, val slug: String, val description: String = "",
    val icon: String = "", val color: String, @SerialName("question_count") val questionCount: Int)

@Serializable data class CategoryBrief(val slug: String, val name: String)

@Serializable data class SessionCreateRequest(val category: String, val platform: String = "android")
@Serializable data class SessionCreateResponse(
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_token") val sessionToken: String,
    val category: CategoryBrief,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("time_limit_seconds") val timeLimitSeconds: Int)

@Serializable data class Choice(val id: Int, val text: String)
@Serializable data class QuestionOut(val id: Int, val text: String)
@Serializable data class CurrentQuestion(
    val index: Int, val total: Int, val question: QuestionOut, val choices: List<Choice>,
    @SerialName("time_limit_ms") val timeLimitMs: Int,
    @SerialName("remaining_ms") val remainingMs: Int)

@Serializable data class AnswerRequest(
    @SerialName("question_id") val questionId: Int,
    @SerialName("choice_id") val choiceId: Int?)   // null => süre doldu (explicitNulls = true gerekli!)
@Serializable data class AnswerResult(
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("correct_choice_id") val correctChoiceId: Int,
    val points: Int,
    @SerialName("response_ms") val responseMs: Int,
    val score: Int, val finished: Boolean)

@Serializable data class SessionResult(
    val score: Int,
    @SerialName("correct_count") val correctCount: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("total_time_ms") val totalTimeMs: Long,
    val category: CategoryBrief)

@Serializable data class ScoreRequest(@SerialName("player_name") val playerName: String)
@Serializable data class LeaderboardEntry(
    val rank: Int, @SerialName("player_name") val playerName: String,
    val score: Int, @SerialName("correct_count") val correctCount: Int)
@Serializable data class ScoreResponse(
    val rank: Int, @SerialName("in_top10") val inTop10: Boolean, val leaderboard: List<LeaderboardEntry>)

@Serializable data class ApiErrorBody(val error: ApiError) {
    @Serializable data class ApiError(val code: String, val message: String)
}
```

**Dikkat — `explicitNulls`:** `Json { explicitNulls = true; ignoreUnknownKeys = true }` kullan.
`encodeDefaults = true` da aç (`platform` varsayılanı gönderilsin). `choice_id: null`
**alanın gövdede `null` olarak bulunması** şeklinde gönderilmelidir; alan atlanırsa backend
`None` kabul eder ama açıkça göndermek web ile birebir aynı davranıştır.

Retrofit arayüzü:

```kotlin
interface RapidQuizApi {
    @GET("categories/") suspend fun categories(): List<Category>
    @POST("sessions/") suspend fun createSession(@Body body: SessionCreateRequest): SessionCreateResponse
    @GET("sessions/{id}/question/") suspend fun question(@Path("id") id: String, @Header("X-Session-Token") token: String): CurrentQuestion
    @POST("sessions/{id}/answers/") suspend fun answer(@Path("id") id: String, @Header("X-Session-Token") token: String, @Body body: AnswerRequest): AnswerResult
    @GET("sessions/{id}/result/") suspend fun result(@Path("id") id: String, @Header("X-Session-Token") token: String): SessionResult
    @POST("sessions/{id}/score/") suspend fun submitScore(@Path("id") id: String, @Header("X-Session-Token") token: String, @Body body: ScoreRequest): ScoreResponse
    @GET("leaderboard/") suspend fun leaderboard(@Query("category") category: String, @Query("limit") limit: Int = 10): List<LeaderboardEntry>
    @GET("health/") suspend fun health(): Map<String, String>
}
```

---

## 7. Oyun mantığı

### 7.1 Akış

```
Kategoriye dokun
  └─ POST sessions/ {category, platform:"android"} → sessionId, token
      └─ döngü (20 kez):
          GET question/ → soru + remaining_ms        (sunucuda sayaç başlar)
          geri sayımı remaining_ms'den başlat
          ├─ kullanıcı şık seçer → POST answers/ {question_id, choice_id}
          └─ sayaç 0'a iner      → POST answers/ {question_id, choice_id: null}
          yanıt → doğru/yanlış renklendir (~800 ms) → finished? sonuç : sonraki soru
  └─ GET result/ → puan, doğru sayısı
      └─ isim gir → POST score/ → Skor Tablosu (kendi satırı vurgulu)
```

### 7.2 Süre ve puan (yalnızca bilgi; hesap sunucuda)

- Soru başına 5000 ms. Sunucu `GET question/` anından itibaren sayar.
- Ağ toleransı 750 ms: `5750 ms`'ye kadar gelen cevap geçerli, sonrası "süre doldu" (0 puan).
- Doğru cevap puanı: `100 + round(100 × (5000 − t) / 5000)` → 100–200 arası; yanlış/boş: 0.
  Maks. skor 20 × 200 = 4000.
- Geri sayım `SystemClock.elapsedRealtime()` ile ölçülür (duvar saati değil); Compose'ta
  `withFrameNanos` ya da `LaunchedEffect` + `delay(16)` döngüsüyle çizilir. Sunucudan gelen
  `remaining_ms` başlangıç değeridir.
- Sayaç görseli: son 2 saniyede `incorrect` rengi (web ile aynı).
- **Gecikme notu:** Geri sayım yanıt geldikten sonra başlar, sunucu ise yanıt gönderildiği
  andan saydığı için istemci bir ağ gecikmesi kadar *geridedir*. Bu yüzden sayaç 0 olunca
  gönderilen `null` cevap her zaman tolerans içindedir (zararsız); ama son anda şık seçen
  yavaş ağdaki kullanıcı 0 puan alabilir — bu, web ile aynı ve kabul edilen davranıştır.

### 7.3 İsim kuralları (istemci ön kontrolü; asıl doğrulama sunucuda)

- `trim()` sonrası 2–20 karakter. Kayıt butonu bu koşulda aktif.
- İzinli: harf (Türkçe dahil), rakam, boşluk, `_`, `-`, `.`.
- Küfür filtresi sunucuda; `400` gelirse `error.message` formun altında gösterilir
  (isteğe bağlı: ön kontrol için aynı regex `^[\p{L}\p{N}_ .\-]+$`).
- Skor kaydedildikten sonra oturum kapatılır (token silinir) → sonuç ekranına geri dönülemez.

---

## 8. Ekranlar ve navigasyon

### 8.1 Rotalar

| Rota | Ekran | Notlar |
|---|---|---|
| `home` | Ana Sayfa | Başlangıç ekranı |
| `quiz/{slug}` | Quiz | Aktif oturum ViewModel'de |
| `result/{slug}` | Sonuç | Aktif oturum yoksa `home`'a yönlendir (web'deki guard'ın karşılığı) |
| `leaderboard/{slug?}` | Skor Tablosu | `slug` verilmezse ilk kategori seçili açılır |

### 8.2 Ekran içerikleri (web ile birebir)

**Ana Sayfa** — "⚡ Rapid Quiz" başlığı, "20 soru, 5 saniye" sloganı, 5 kategori kartı
(emoji + ad + açıklama + soru sayısı, kategori rengiyle), altta "🏆 Skor Tablosu" butonu.
Yükleniyor: iskelet kartlar. Hata: "Kategoriler yüklenemedi" + **Tekrar Dene**.

**Quiz** — üstte kategori adı + anlık puan (⭐), ilerleme çubuğu + `n/20`, ortada dairesel
5 sn sayaç, soru kartı, 4 büyük şık butonu (A–D). Cevap sonrası ~800 ms geri bildirim:
doğru şık yeşil, seçilen yanlış kırmızı (+ sallanma), süre dolduysa "⏰ Süre doldu!".
Geri bildirim sırasında dokunma kilitli. Hata: "Tekrar Dene" / "Ana Sayfa".
**Geri tuşu:** "Oyundan çıkılsın mı?" onayı; onaylanırsa oturum bırakılır (sunucuda
180 sn sonra kendiliğinden `expired` olur, ayrıca silme ucu yok).

**Sonuç** — "Quiz bitti!" (≥15 doğruysa "🎉 Harika iş!" + konfeti), puan sayarak artan
animasyon, `x / 20 doğru`, isim alanı (2–20), **Kaydet**, "Kaydetmeden Tekrar Oyna".
Reduced-motion (`Settings.Global.ANIMATOR_DURATION_SCALE == 0`) açıkken animasyon/konfeti yok.

**Skor Tablosu** — yatay kaydırılan kategori sekmeleri, top 10 listesi (ilk 3 için 🥇🥈🥉),
sıra / isim / puan / doğru sayısı; az önce kaydedilen skor ilk 10'daysa o satır vurgulu
(`ScoreResponse.rank`, yalnızca bellekte). Altta "🔁 Tekrar Oyna", üstte "Ana Sayfa".

### 8.3 Tasarım token'ları (web `tailwind.config.ts` ile aynı)

| Token | Hex | Kullanım |
|---|---|---|
| brand | `#7C3AED` | Ana renk, butonlar, başlık |
| secondary | `#06B6D4` | İlerleme çubuğu, bağlantılar |
| accent | `#FACC15` | Puan rozeti, madalyalar |
| correct | `#22C55E` | Doğru şık |
| incorrect | `#F43F5E` | Yanlış şık, son 2 sn |
| ink | `#1E1B4B` | Ana metin |
| arka plan | `#FFF7ED → #FDF2F8` (dikey gradyan) | Ekran zemini |

Köşe yarıçapı: kart 24 dp, kontrol 16 dp. Font: Plus Jakarta Sans (Google Fonts, `res/font`
içine gömülür; ağdan indirilmez). Kategori rengi API'den (`color`) gelir; boşsa slug'a göre yedek.
İkon adı → emoji: `code 💻`, `brain 🧠`, `cpu 🖥️`, `globe 🌍`, `atom ⚛️`, bilinmeyen `✨`.
Dokunma hedefi ≥ 48 dp; her etkileşimli öğe için `contentDescription`/semantics.

---

## 9. Hata ve kenar durumları

| Durum | Davranış |
|---|---|
| **Sunucu uyuyor (Render)** | Açılışta arka planda `health/` çağır (sonuç beklenmez). Kategori yüklemesi 5 sn'den uzun sürerse "Sunucu uyanıyor, ~1 dk sürebilir…" metni göster; zaman aşımında **Tekrar Dene**. |
| İnternet yok | `UnknownHostException`/`SocketTimeout` → "Bağlantını kontrol et" + Tekrar Dene. Quiz ortasındaysa sayaç durdurulur. |
| Cevap isteği ağ hatasıyla koptu | Aynı isteği **otomatik yeniden gönderme**. Önce `GET question/` ile durumu doğrula: soru ilerlemişse yanıt kaybolmuştur (puan sunucuda işlenmiştir) → devam et; ilerlememişse kalan süre varsa tekrar gönder. `409 already_answered` / `question_mismatch` aynı yolu izler. |
| Uygulama arka plana alındı | Sayaç sunucuda akmaya devam eder. Geri gelince `GET question/` ile `remaining_ms` yeniden alınır ve sayaç ona eşitlenir; süre bittiyse `null` cevap gönderilir. 180 sn aşılırsa `410` → "Oturum süresi doldu". |
| Süreç öldürüldü / uygulama kapandı | Oturum kaybolur (altın kural 1). Yeniden açılışta Ana Sayfa. |
| Ekran döndürme | Yok (dikey kilitli); yine de oturum `ViewModel`'de tutulur, `rememberSaveable` ile token saklanmaz. |
| Çift dokunma | `locked` bayrağı: bir cevap gönderilirken yeni dokunuş yok sayılır. |
| `429` | Mesaj göster, 2–3 sn sonra tekrar denemeye izin ver; otomatik döngüye girme. |

---

## 10. Proje yapısı

```
app/src/main/java/com/rapidquiz/
  RapidQuizApp.kt          # Application + AppContainer (Retrofit, repository)
  MainActivity.kt          # setContent { RapidQuizTheme { AppNavHost() } }
  data/
    api/        RapidQuizApi.kt, ApiClient.kt (OkHttp/Retrofit kurulumu), dto/*.kt
    repo/       QuizRepository.kt     # API çağrıları + hata → ApiResult dönüşümü
  domain/       QuizSession.kt        # sessionId, token, kategori, puan (yalnızca bellek)
  ui/
    theme/      Color.kt, Type.kt, Theme.kt
    nav/        AppNavHost.kt
    home/       HomeScreen.kt, HomeViewModel.kt
    quiz/       QuizScreen.kt, QuizViewModel.kt, CountdownRing.kt, ChoiceButton.kt
    result/     ResultScreen.kt, ResultViewModel.kt, Confetti.kt
    leaderboard/ LeaderboardScreen.kt, LeaderboardViewModel.kt
    common/     ErrorCard.kt, LoadingSkeleton.kt, CategoryStyle.kt (emoji/renk)
app/src/test/ …  # birim testleri
app/src/androidTest/ … # Compose UI testi
```

**Oturum paylaşımı:** Quiz → Sonuç ekranları aynı oturumu kullanır. `QuizSessionHolder`
(Application seviyesinde, yalnızca bellek, `StateFlow<QuizSession?>`) üzerinden paylaşılır;
diske yazılmaz. Skor kaydedilince `clear()` çağrılır.

---

## 11. Geliştirme aşamaları

| Aşama | Kapsam | Bitiş ölçütü |
|---|---|---|
| **M1 — İskelet** | Android Studio projesi, Compose + tema/renkler/font, Navigation (4 boş ekran), Retrofit/OkHttp/serialization, DTO'lar, `BuildConfig` adresleri, network security config | Uygulama açılır, `GET categories/` canlı backend'den başarıyla okunur (log) |
| **M2 — Ana Sayfa + Skor Tablosu** | Kategori kartları, kategori sekmeli top 10, yükleme/hata/uyanıyor durumları | Canlıda gerçek veriyle iki ekran çalışır |
| **M3 — Quiz** | Oturum başlatma, soru döngüsü, geri sayım, cevap, geri bildirim, geri tuşu onayı, kenar durumları (Bölüm 9) | 20 soru uçtan uca oynanır; `null` cevap ile süre dolumu doğrulanır |
| **M4 — Sonuç + Kaydet** | Sonuç ekranı, sayma animasyonu, konfeti, isim formu, skor kaydı, vurgulu satır | Oyun → sonuç → kayıt → tabloda satır görünür |
| **M5 — Test ve cila** | Birim + MockWebServer + Compose testleri, erişilebilirlik (TalkBack), reduced-motion, küçük ekran (360 dp) taşma kontrolü, ikon/uygulama adı | Testler yeşil, gerçek cihazda elle tam akış |
| **M6 — Yayın** | İmzalı release APK/AAB, R8, sürüm numaraları | Bölüm 12 |

Her aşama testleri geçmeden kapatılmaz (backend/web ile aynı çalışma şekli).

### Test planı

- **Birim:** `QuizViewModel` durum makinesi (soru → cevap → geri bildirim → sonraki/bitti),
  `null` cevap yolu, `409` yeniden-senkronizasyon, hata kodu → mesaj eşlemesi, isim ön kontrolü.
- **API sözleşmesi:** MockWebServer ile her uç için gerçek JSON örnekleri (Bölüm 5.3) ayrıştırılır;
  `choice_id: null`'ın gövdede `null` gittiği ve `X-Session-Token`'ın eklendiği doğrulanır.
- **UI (Compose):** Ana Sayfa kartları, Quiz'de şık seçimi kilidi, Sonuç'ta geçersiz isimle
  butonun pasif kalması.
- **Elle:** Gerçek cihaz + canlı backend; uçak modu, arka plana alma, uyuyan sunucu.
- **Canlı duman testi** (isteğe bağlı): web'deki `E2E_LIVE` benzeri tek bir test sınıfı.

---

## 12. Yayın ve dağıtım

- Kullanıcının ödeme yöntemi olmadığı için **Google Play (tek seferlik 25 USD) şimdilik
  kapsam dışı**. Önerilen yol: imzalı **release APK**'yı GitHub Releases'ten dağıtmak
  (kullanıcı "bilinmeyen kaynaklardan yükle"yi açar). Play Store sonradan istenirse AAB aynı
  projeden üretilir.
- **Keystore** repoya girmez; parolası/dosyası `local.properties` / CI secret'ında tutulur,
  sohbette paylaşılmaz.
- `release` build: R8/minify açık, `API_BASE_URL` canlı adres, logging kapalı.
- Uygulama kimliği önerisi: `com.rapidquiz.android`; ad: **Rapid Quiz**.
- Depo: ayrı repo `RapidQuizAndroid` (backend ve web gibi kardeş repo), master dalına push.
  CI (GitHub Actions): `./gradlew lint test assembleDebug`.
- Backend adresi değişirse tek yer: `BuildConfig.API_BASE_URL` (web'deki gibi CSP gibi ikinci
  bir sabit yok).

---

## 13. Açık sorular

1. **"Genel scoreboard" = kategori sekmeli tablo mu, yoksa tüm kategorileri birleştiren tek liste mi?**
   Varsayılan: web ile aynı (kategori sekmeli) → backend değişikliği yok. Birleşik liste istenirse
   backend'e `category` parametresi opsiyonel yapılıp tüm kategoriler üzerinden sıralayan bir
   mod eklenmeli (puanlar kategoriler arasında karşılaştırılabilir olsa da bunun adil olup
   olmadığına karar verilmeli).
2. **Dağıtım:** APK (GitHub Releases) yeterli mi, Play Store ileride gerekecek mi?
3. **Uygulama adı / ikon:** "Rapid Quiz" ve ⚡ temalı basit bir ikon mu?
4. **Bildirilen mevcut (mobille ilgisiz) risk:** Render uykusu ilk açılışta ~1 dk bekletir;
   kabul edilebilir mi, yoksa ücretsiz bir "uyanık tutma" ping'i düşünülsün mü?
