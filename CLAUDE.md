# CLAUDE.md — RapidQuizAndroid

Rapid Quiz'in Android istemcisi (web: `../RapidQuizFrontend`, backend: `../RapidQuizBackend`).
Tam spesifikasyon: `docs/RapidQuiz-Mobil-Proje-Dokumani.md` — önce onu oku.

## Yığın
Kotlin · Jetpack Compose (Material 3) · MVVM (ViewModel + StateFlow) · Navigation Compose ·
Retrofit + OkHttp + kotlinx.serialization · elle DI (`AppContainer`) · tek modül `app`.
Paket `com.rapidquiz`, applicationId `com.rapidquiz.android`, minSdk 26, yalnızca dikey.

## Altın kurallar (docs Bölüm 4)
1. Token yalnızca bellekte (`QuizSessionHolder`); SharedPreferences/DataStore/dosya yok. Token log'a yazılmaz.
2. Doğru cevap istemcide hesaplanmaz; `correct_choice_id` yalnızca `answers/` yanıtından gelir.
3. Süre sunucuda ölçülür; sayaç görseldir, tek yaptığı süre bitince `choice_id = null` göndermek.
4. Oturum `X-Session-Token` başlığıyla taşınır. Yalnızca HTTPS (düz HTTP sadece debug + `10.0.2.2`).

## Komutlar
Windows'ta `JAVA_HOME` Android Studio JBR'ına ayarlanır: `C:\Program Files\Android\Android Studio\jbr`.

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
RQ_LIVE=1 ./gradlew testDebugUnitTest --rerun     # canlı backend duman testi
./gradlew assembleDebug -PdebugApiBaseUrl=http://10.0.2.2:8000/api/v1/   # yerel backend
```

## Durum
- M1 İskelet ✅ — proje, tema/renkler, navigasyon (4 taslak ekran), Retrofit/DTO/repository,
  network security config, birim + MockWebServer testleri, canlı `categories/` doğrulandı.
  Font (Plus Jakarta Sans) henüz gömülmedi (`ui/theme/Type.kt` TODO).
- M2 Ana Sayfa + Skor Tablosu ✅ — kategori kartları, iskelet/hata/"sunucu uyanıyor" durumları, kategori sekmeli top 10, vurgulu satır (`QuizSessionHolder.lastScore`), ViewModel testleri; emülatörde canlı backend ile doğrulandı.
- M3…M6: docs Bölüm 11.
