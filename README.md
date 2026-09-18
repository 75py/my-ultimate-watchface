# My Ultimate Watch Face

Google Pixel Watch（円形ディスプレイ）向けの Watch Face Format (WFF) ウォッチフェイスです。
時刻と日常・健康情報を一目で確認できる、黒背景ベースのデジタルウォッチフェイスです。

- アプリ名: My Ultimate Watch Face
- Application ID / namespace: `com.nagopy.android.myultimatewatchface`
- リポジトリ: `my-ultimate-watchface`
- スマートフォン側アプリなし（ウォッチ単体のリソース専用パッケージ）

`sample.png`（プロジェクトルート）を完成イメージとして実装しています。

## 技術構成

| 項目 | 値 |
|---|---|
| Watch Face Format | バージョン 2 |
| 対応 Wear OS | Wear OS 5 以降 |
| minSdkVersion | 34 |
| compileSdkVersion / targetSdkVersion | 36 |
| Gradle | 9.2.1（Wrapper） |
| Android Gradle Plugin | 9.0.0 |
| 言語 | Kotlin コードなし（`android.enableKotlin=false`、リソース専用） |

WFF v2 は Wear OS 5 / API 34 以降で利用できます。天気データソース（`[WEATHER.*]`）と
GOAL_PROGRESS を含む編集可能な ComplicationSlot、時刻の桁別表示（`TimeText`）が必須要件の
ため、それらを満たす最小バージョンとして v2 を採用しました
（WFF v3 は Wear OS 5.1 / API 35、v4 は Wear OS 6 / API 36 向けです）。

`AndroidManifest.xml` では `android:hasCode="false"` と
`com.google.wear.watchface.format.version=2` のプロパティを設定しています。
廃止された `WatchFaceService` は使用していません。

## 表示仕様

### 通常表示（インタラクティブ）

| 位置（時計位置） | 項目 |
|---|---|
| 11-1時（上部中央） | 月・日・曜日と、その下に天気（現在気温＋最高/最低気温＋天候名を1領域に集約） |
| 中央 | HH:MM（最も大きく、幾何学的中心に固定・桁別配置で横揺れ抑制・24時間制）＋右横に秒（独立要素、通常表示のみ） |
| 9-11時（左上） | 心拍数（ピンクの円弧＋ハートアイコン＋値＋bpm） |
| 1-3時（右上） | 次回アラーム（藍色の円弧＋アイコン＋時刻＋アラームラベル、編集可能スロット） |
| 7-9時（左下） | 歩数（緑の円弧＋アイコン＋歩数/目標値） |
| 5-7時（下中央） | 距離（青の円弧＋アイコン＋値、編集可能スロット） |
| 3-5時（右下） | 消費カロリー（橙の円弧＋アイコン＋値、編集可能スロット） |

外周には5本の円弧を配置。歩数のみ `[STEP_PERCENT]` による進捗連動です
（WFF式はコンプリケーション値をスロット外で参照できないため、他の円弧は
静的なアクセント円弧です。目標達成率の捏造を避けるため進捗のふりはしません）。

### AOD（常時表示）

- 時分と日付のみ表示します。HH:MM の座標・サイズは通常表示と完全に同一です。
- 秒・天気・心拍・アラーム・運動情報・円弧・目盛りは非表示です。
- 黒背景＋減光表示で省電力化。秒単位のアニメーションはありません。

## データ取得方法

| 項目 | 取得方法 | 必要WFF | データ型 | 提供元依存 | 未取得時 |
|---|---|---|---|---|---|
| 月・日・曜日 | `[DAY]` `[MONTH]` `[DAY_OF_WEEK_S]` | v1+ | 数値/文字列 | なし（OS標準） | —（常に取得可） |
| 時・分 | `DigitalClock`＋`TimeText`（`[HOUR_0_23]`/`[MINUTE]`） | v1+ | 数値 | なし | — |
| 秒 | `[SECOND_Z]`（インタラクティブのみ） | v1+ | 数値 | なし | AODでは非表示 |
| 天気 | `[WEATHER.*]`（IS_AVAILABLE/TEMPERATURE/DAY_TEMPERATURE_HIGH/LOW/CONDITION/CONDITION_NAME/HOURS） | v2+ | 構造化データ | Wear OS の天気データソース（Googleアプリ等） | `--°`（IS_AVAILABLE/IS_ERROR で出し分け） |
| 心拍数 | `[HEART_RATE]` | v1+ | 数値（約1分間隔の最新値） | ウォッチ本体のHRセンサー | `--`（1未満の場合） |
| 次回アラーム | ComplicationSlot #10（SHORT_TEXT） | v2+ | 短いテキスト | 時計アプリ等の「次のアラーム」コンプリケーションをユーザーが割当 | `未設定`（EMPTY時） |
| 距離 | ComplicationSlot #11（GOAL_PROGRESS/SHORT_TEXT） | v2+ | コンプリケーション | Fitbit/Health Connect連携アプリ等をユーザーが割当 | `--`（EMPTY時） |
| 歩数 | `[STEP_COUNT]` / `[STEP_GOAL]` | v1+ | 数値 | ウォッチ本体の歩数計 | `0` は実値として表示（未取得は発生しない前提） |
| 消費カロリー | ComplicationSlot #12（GOAL_PROGRESS/SHORT_TEXT） | v2+ | コンプリケーション | Fitbit/Health Connect連携アプリ等をユーザーが割当 | `--`（EMPTY時） |

注意点：

- アラーム・距離・消費カロリーに対応する WFF 標準タグは存在しないため、
  **編集可能な ComplicationSlot** を用意し、利用者が提供元を選択する方式です。
  初期状態は EMPTY（アラームは `未設定`、距離・カロリーは `--` 表示）です。
  データソースとして特定アプリ名やコンポーネント名はプリセットしていません。
- 「アラームなし」と「未取得」の区別は提供元のテキスト内容に依存します。
  アラームスロットがEMPTY（プロバイダ未割当）の場合は `未設定` と表示し、
  タップするとアラーム設定アプリが開きます。
- 距離の集計対象（徒歩距離のみか等）・消費カロリーが活動分か総消費かは、
  割り当てたコンプリケーション提供元の仕様に従います。
- 歩数の円弧のみ `[STEP_PERCENT]` で進捗連動し、`clamp()` で表示範囲に収めます。
  目標値なし/ゼロ除算の場合は中立的なトラック表示です。距離・カロリー・心拍・
  アラームの円弧は静的なアクセント表示です。

## タップ動作

時刻・秒以外の各項目をタップすると対応するアプリ/画面を開きます（WFF `Launch` 要素）。

| 領域 | 遷移先 |
|---|---|
| 日付 | カレンダーアプリ（システムターゲット `CALENDAR`） |
| 天気 | Google Weatherアプリ（`com.google.android.wearable.weather`。未インストール時は Play Store のインストール画面が開きます。Wear OS 6 で Pixel Weather に置き換わった端末では `com.google.android.apps.weather` への変更を検討してください） |
| 心拍数 | デフォルトの心拍計測アプリ（システムターゲット `HEALTH_HEART_RATE`。Pixel Watch では Fitbit） |
| 歩数 | Fitbit（Google Health）アプリ（`com.fitbit.FitbitMobile`。未インストール時は Play Store のインストール画面） |
| アラーム（プロバイダ割当済み） | プロバイダ自身のタップアクション（通常はアラームアプリ） |
| アラーム（EMPTY=未割当） | `未設定` と表示され、アラーム設定アプリ（システムターゲット `ALARM`）を開きます |
| 距離・消費カロリー（プロバイダ割当済み） | プロバイダ自身のタップアクション |
| 距離・消費カロリー（EMPTY=未割当） | Fitbit アプリを開きます |

## ビルド方法

前提: JDK 17 以上、Android SDK（cmdline-tools、platform-tools、build-tools、platforms;android-36）。
`local.properties` の `sdk.dir` に SDK パスを書くか `ANDROID_HOME` を設定してください。

```bash
# デバッグAPK（デバッグ署名済み）
./gradlew :watchface:assembleDebug
# 出力: watchface/build/outputs/apk/debug/watchface-debug.apk

# リリースAAB（後述の署名設定が必要。未設定時はデバッグ鍵で署名され警告が出ます）
./gradlew :watchface:bundleRelease
# 出力: watchface/build/outputs/bundle/release/watchface-release.aab
```

### リリース署名の設定

署名情報は Git 管理外にしてください。次のどちらかを用意します。

1. `keystore.properties.template` を `keystore.properties`（リポジトリ直下）にコピーして記入
   （`.gitignore` 済み）
2. 環境変数 `MUWF_KEYSTORE_PATH` / `MUWF_KEYSTORE_PASSWORD` / `MUWF_KEY_ALIAS` /
   `MUWF_KEY_PASSWORD` を設定

未設定の場合、リリースビルドはデバッグ鍵での署名にフォールバックします
（Play Store 配布には使えません）。

## インストールと文字盤選択

```bash
adb install -r watchface/build/outputs/apk/debug/watchface-debug.apk
```

1. ウォッチ上で文字盤を長押し
2. 一覧の末尾の「新しいウォッチフェイスを追加」→ グリッド内の「My Ultimate Watch Face」を選択

## Complication の設定（アラーム・距離・消費カロリー）

1. ウォッチ上で文字盤を長押し → 鉛筆アイコン（編集）
2. 各スロットをタップして提供元を選択
   - 右上のスロット: 時計アプリ等の「次のアラーム」（SHORT_TEXT）
   - 下中央のスロット: 距離（GOAL_PROGRESS または SHORT_TEXT）
   - 右下のスロット: 消費カロリー（GOAL_PROGRESS または SHORT_TEXT）
3. 提供元が見つからない場合は `--` 表示のままになります。
   ウォッチ上の Fitbit アプリや対応アプリの提供状況をご確認ください。

## Google Play 内部テストでの配布

1. （初回のみ）アップロード鍵を生成し、上記の署名設定を行う。
   ※本リポジトリには鍵は含まれていません。Play Console で「Play アプリ署名」に
   登録するアップロード鍵は各自で用意してください。
2. `./gradlew :watchface:bundleRelease` で AAB を生成。
3. Play Console でアプリを作成 → テストとリリース → 内部テスト。
4. `watchface-release.aab` をアップロードしリリースを作成。
5. テスターを登録し、招待リンクを共有。テスターのウォッチに
   Play のウォッチ向けインストール経路で配布されます。

## 検証済み事項 / 未検証事項

検証済み（本リポジトリ作成時点）:

- `./gradlew :watchface:assembleDebug` / `:watchface:bundleRelease` が成功
- 公式 WFF バリデータ（フォーマットバージョン 2）: **PASSED**
- 公式メモリフットプリント検証（active ≤100MB / ambient ≤10MB）: **PASSED**
- APK の署名（v2、Android Debug 証明書）と `aapt2` によるパッケージ情報の確認
- Wear OS 5 エミュレータ（円形）での通常表示・AOD 表示のスクリーンショット確認
- 通常表示/AOD で HH:MM の座標・サイズが同一であること
- 天気/心拍/距離/カロリー未取得時の `--`、アラーム未割当時の `未設定` フォールバック表示
- `sample.png` との比較による配置・配色・情報優先順位の整合
- エミュレータで3スロットへプロバイダ割当 → 値の描画を確認（アラーム=Clock、距離/カロリー=テスト用プロバイダ）
- タップ動作：日付→カレンダー、天気→Play Store誘導（エミュレータに天気アプリ無し）、歩数→Play Store誘導、
  アラーム未設定→アラームアプリ、アラーム割当済み→アラーム設定画面（プロバイダのタップアクション）、
  距離EMPTY→Fitbitインストール誘導

未検証:

- 実機（Pixel Watch）での表示・消費電力・実センサー値
- `HEALTH_HEART_RATE` の起動（エミュレータにデフォルト心拍アプリがなく未確認。Pixel Watch では Fitbit が開く想定）
- 実際の天気データ・心拍・距離/カロリー用プロバイダ（Fitbit 等）割当時の表示
- 5桁以上の歩数など大きな数値での折返し・はみ出し（式は `%,d`・`clamp` 済み）
- AAB の Play Console アップロード（アップロード鍵未作成のため）

## `sample.png` からの主な調整点

- 中央の時刻は本文要件に従い HH:MM を幾何学的中心に固定し、秒は右横に独立配置。
- 各項目はサンプル同様に時計位置へ配置（心拍9-11時・アラーム1-3時・歩数7-9時・
  距離5-7時・消費カロリー3-5時）。外周は5本の円弧で統一。
- 「アラーム」はスロット内の値の下に表示。「歩数」「距離」「消費カロリー」の
  文字ラベルは配色とアイコンで識別するため付けていません。
- アラーム/距離/カロリーは編集可能なコンプリケーションスロットで実装
  （WFFに対応する標準データタグがないため）。進捗連動ができない距離/カロリーの
  円弧は中立的なトラック表示のみ（サンプルの部分充填アークは再現せず、
  達成率を捏造しない）。
- 「km」「kcal」の単位ラベルは割り当てた提供元のテキストに含まれる前提で
  固定表示していません（重複表示を避けるため）。

## ファイル構成

```
sample.png                      # 参照デザイン画像（変更禁止）
settings.gradle.kts
build.gradle.kts                # ルートビルド設定
gradle/libs.versions.toml       # バージョンカタログ（AGP 9.0.0）
gradle/wrapper/                 # Gradle 9.2.1 Wrapper
keystore.properties.template    # リリース署名設定サンプル
watchface/
  build.gradle.kts              # WFFモジュール設定（hasCode=false想定）
  src/main/
    AndroidManifest.xml         # hasCode=false, format.version=2, publisher
    res/
      raw/watchface.xml         # ウォッチフェイス定義（450x450）
      xml/watch_face_info.xml   # プレビュー・編集可能宣言
      drawable/                 # アイコン・プレビュー画像
      font/                     # Poppins（時刻用）
      values/strings.xml
```
