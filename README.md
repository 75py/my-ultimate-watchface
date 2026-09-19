# My Ultimate Watch Face

Google Pixel Watch（円形ディスプレイ）向けの Watch Face Format (WFF) ウォッチフェイスです。
時刻と日常・健康情報を一目で確認できる、黒背景ベースのデジタルウォッチフェイスです。

- アプリ名: My Ultimate Watch Face
- Application ID / namespace: `com.nagopy.android.myultimatewatchface`
- リポジトリ: `my-ultimate-watchface`
- スマートフォン側アプリなし（ウォッチ単体のリソース専用パッケージ）

デザインは Figma で作成したパステル配色のレイアウト（Watch Face Designer 形式でエクスポート済み）を
ベースに、太字の時刻・進捗連動のセグメントリング・Material Symbols アイコンでスタイルを整えたものです。

## スクリーンショット

`docs/screenshots/` の画像は `watchface.xml` と同じ座標を Pillow で再現したモックレンダリングです
（システムフォントは Roboto / Noto Sans JP で代用。実機・エミュレータのキャプチャではありません）。

| 通常表示（ミント・既定） | ブルー | オレンジ | AOD |
|---|---|---|---|
| ![通常表示 ミント](docs/screenshots/interactive_mint.png) | ![ブルー](docs/screenshots/interactive_blue.png) | ![オレンジ](docs/screenshots/interactive_orange.png) | ![AOD](docs/screenshots/ambient.png) |

極端な値（23:58、氷点下、長い天候名、5桁の歩数、目標達成、心拍未取得、アラーム未設定）の確認用:
`docs/screenshots/interactive_edge_cases.png`

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
| 11-1時（上部中央） | 月・日・曜日と、その下に天気（天気アイコン＋現在気温＋最高/最低気温＋天候名を1領域に集約） |
| 中央 | HH:MM（Inter Bold ビットマップフォント・サイズ96・横方向は幾何学的中心に固定・桁別配置で横揺れ抑制・24時間制）＋右横に秒（Inter Regular、ベースライン揃え、通常表示のみ） |
| 10時（左上） | 心拍数（ハートアイコン＋値＋bpm ラベル） |
| 2時（右上） | 次回アラーム（アイコン＋時刻、編集可能スロット） |
| 8時（左下） | 歩数（シューズアイコン＋歩数値＋`/ 目標値`） |
| 6時（下中央） | 距離（ピンアイコン＋値＋km ラベル、編集可能スロット） |
| 4時（右下） | 消費カロリー（炎アイコン＋値＋kcal ラベル、編集可能スロット） |

数値は白（SemiBold）、アイコンと時刻はテーマのアクセント色、単位ラベルはグレーという
3段階の階調で情報の優先順位を付けています。

外周には半径204の円周上に5本のセグメント（各48°、上部が開いた C 字型）を
`PartDraw` の `Arc` で描画しています。各セグメントはテーマのトラック色（暗色）を
下地に、アクセント色で次のように点灯します。

| セグメント（角度、0°=12時） | 点灯 |
|---|---|
| 心拍 276–324° | 常時点灯（装飾） |
| アラーム 36–84° | 常時点灯（装飾） |
| 歩数 216–264° | `[STEP_PERCENT]` による実進捗（目標未設定時は下地のみ） |
| 距離 156–204° | 割り当てた GOAL_PROGRESS コンプリケーションの実進捗（値/目標。それ以外は下地のみ） |
| 消費カロリー 96–144° | 同上 |

点灯部の下には幅20・alpha 40 の同色アークを敷き、柔らかな発光感を出しています。
進捗のない項目を「途中まで点灯」させて達成率を捏造することはしません。

### AOD（常時表示）

- HH:MM のみ表示します。桁ボックスの座標・サイズは通常表示と完全に同一で、
  ウェイトのみ Inter Regular（通常表示は Bold）にし、テーマ色を alpha 150 で減光します。
- 日付・秒・天気・心拍・アラーム・運動情報・リングは非表示です。
- 黒背景＋減光表示で省電力化。秒単位のアニメーションはありません。

### テーマカラー（ユーザー設定）

`UserConfigurations` の `ColorConfiguration` で3色のパステルテーマを用意しています。
文字盤を長押し → 鉛筆アイコン（編集）→「テーマカラー」で切り替えられます。

| テーマ | アクセント（前景） | トラック（リングの下地） |
|---|---|---|
| ミント（既定） | `#ffbdfff1` | `#ff1f3a33` |
| ブルー | `#ffbbdfff` | `#ff223549` |
| オレンジ | `#ffffd4af` | `#ff45311f` |

アクセント色は時刻・秒・アイコン・リングの点灯部と発光に、トラック色はリングの下地に
適用されます。各数値（白）、「bpm」「km」「kcal」などのグレーラベル、天気アイコン
（カラー画像）はティント対象外です。

## データ取得方法

| 項目 | 取得方法 | 必要WFF | データ型 | 提供元依存 | 未取得時 |
|---|---|---|---|---|---|
| 月・日・曜日 | `[DAY]` `[MONTH]` `[DAY_OF_WEEK_S]` | v1+ | 数値/文字列 | なし（OS標準） | —（常に取得可） |
| 時・分 | `DigitalClock`＋`TimeText`（`[HOUR_0_23]`/`[MINUTE]`） | v1+ | 数値 | なし | — |
| 秒 | `[SECOND_Z]`（インタラクティブのみ） | v1+ | 数値 | なし | AODでは非表示 |
| 天気 | `[WEATHER.*]`（IS_AVAILABLE/TEMPERATURE/DAY_TEMPERATURE_HIGH/LOW/CONDITION/CONDITION_NAME/HOURS） | v2+ | 構造化データ | Wear OS の天気データソース（Googleアプリ等） | `--°`（IS_AVAILABLE/IS_ERROR で出し分け） |
| 心拍数 | `[HEART_RATE]` | v1+ | 数値（約1分間隔の最新値） | ウォッチ本体のHRセンサー | `--`（1未満の場合） |
| 次回アラーム | ComplicationSlot #10（SHORT_TEXT） | v2+ | 短いテキスト | 時計アプリ等の「次のアラーム」コンプリケーションをユーザーが割当 | `未設定`（EMPTY時） |
| 距離 | ComplicationSlot #11（GOAL_PROGRESS/SHORT_TEXT）。GOAL_PROGRESS 時は `[COMPLICATION.GOAL_PROGRESS_VALUE]` / `[COMPLICATION.GOAL_PROGRESS_TARGET_VALUE]` でリングを点灯 | v2+ | コンプリケーション | Fitbit/Health Connect連携アプリ等をユーザーが割当 | `--`（EMPTY時） |
| 歩数 | `[STEP_COUNT]` / `[STEP_GOAL]` / `[STEP_PERCENT]` | v1+ | 数値 | ウォッチ本体の歩数計 | `0` は実値として表示（未取得は発生しない前提）。目標なしの場合は `/ 目標` とリング点灯を省略 |
| 消費カロリー | ComplicationSlot #12（GOAL_PROGRESS/SHORT_TEXT）。GOAL_PROGRESS 時は同上でリングを点灯 | v2+ | コンプリケーション | Fitbit/Health Connect連携アプリ等をユーザーが割当 | `--`（EMPTY時） |

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
- WFF式はコンプリケーション値をスロット外で参照できないため、距離・消費カロリーの
  リングは **ComplicationSlot の内側**に `PartDraw` を置いて描画しています。
  スロットの矩形は該当セグメントを含む大きさ（距離: 130,200–320,450 / カロリー:
  200,200–450,450）にし、タップ・編集時のハイライト領域は `BoundingBox` で数値部分のみに
  絞っています。心拍・アラームのセグメントは常時点灯の装飾です。

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
4. 距離・消費カロリーに GOAL_PROGRESS 型の提供元を割り当てると、外周リングの該当
   セグメントが目標達成率に応じて点灯します（SHORT_TEXT 型では下地のみ）。

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

検証済み（スタイル調整版時点）:

- 公式 WFF バリデータ（google/watchface の `third_party/wff/specification/validator` を
  ソースからビルド、フォーマットバージョン 2）: **PASSED**
- XML が参照する drawable リソースがすべて存在し、未参照の drawable がないこと
- `preview.png` と `docs/screenshots/*.png` は XML と同じ座標を Pillow で再現したモックレンダリング
  （システムフォントは Roboto / Noto Sans JP で代用）

未検証（Figma 版では検証済みだったが、スタイル調整後は再確認が必要な項目を含む）:

- `./gradlew :watchface:assembleDebug`（作業環境に Android SDK がなく未実行）
- 公式メモリフットプリント検証（active ≤100MB / ambient ≤10MB）。
  未使用の Inter グリフ（A/P/M/a/p/m/./-）と円弧・ベゼル画像を削除したため、
  Figma 版より使用量は減っている見込み
- Wear OS 5 エミュレータ／実機（Pixel Watch）での通常表示・AOD 表示、消費電力、実センサー値
- `weight="SEMI_BOLD"` 等の `SYNC_TO_DEVICE` フォントのウェイト指定が端末で反映されること
- ComplicationSlot 内の `PartDraw` による GOAL_PROGRESS 進捗アークの描画、および
  スロット矩形より小さい `BoundingBox` がタップ領域として機能すること
- ブルー/オレンジテーマの実機描画
- `HEALTH_HEART_RATE` の起動（エミュレータにデフォルト心拍アプリがなく未確認。Pixel Watch では Fitbit が開く想定）
- 実際の天気データ・心拍・距離/カロリー用プロバイダ（Fitbit 等）割当時の表示
- 5桁以上の歩数など大きな数値での折返し・はみ出し
- 距離/カロリーのプロバイダが単位（km/kcal）をテキストに含める場合、
  固定ラベルと重複表示になる可能性
- AAB の Play Console アップロード（アップロード鍵未作成のため）

## スタイル調整（Figma 版からの変更点）

- 時刻を Inter Bold・サイズ96 に拡大し、横方向を幾何学的中心（x=225）に固定。
  秒は Inter Regular（サイズ30）で時刻のベースラインに揃え、alpha 190 で一段控えめに。
- 外周の細い円弧画像（`arc_*.png`）・ベゼル・目盛り画像を廃止し、`PartDraw` の `Arc` に
  よる5本のセグメントリング（下地＋点灯＋発光）に置き換え。歩数・距離・カロリーは
  実データで進捗連動（前述）。
- アイコンを Figma 由来の 19–22px 画像から Material Symbols Rounded（Filled, wght 500、
  Apache License 2.0）を 96px にラスタライズした白 PNG に差し替え、28px で表示。
  6枚重ねだった歩数アイコンは1枚に統合。
- 「bpm」「km」「kcal」のラベル画像をテキスト（`SYNC_TO_DEVICE`、グレー）に置き換え、
  歩数には `/ 目標値` を追加。
- 数値を白の SemiBold に変更し、アクセント色は時刻・アイコン・リングに限定して階調を明確化。
- 天気の最高/最低気温の表示幅を 43→60px に拡大（`%d° / %d°` が省略記号にならないように）。
- `preview.png` を実際の時刻（10:08:36）とサンプル値で再生成（Figma 版は `Hh:Mm` の
  プレースホルダーのままだった）。
- 未使用の Inter グリフ画像（A/P/M/a/p/m/./-）を削除し BitmapFont の宣言も 0–9 と `:` のみに。
- `STEP_GOAL` メタデータ（既定 10,000）を再追加。

## Figma デザイン適用での主な変更点

- デザインソースを `sample.png` から Figma エクスポート（Watch Face Designer 形式）に変更。
  配置・サイズ・配色は Figma の座標（450x450 キャンバス）を忠実に再現しています。
- フォントを Poppins（TTF）から Inter ビットマップフォント（Regular/Bold、Figma エクスポート由来）に変更。
  時刻・秒・各数値に適用。日付と天気の「天候名/最高最低気温」はシステムフォントです。
- テーマカラー設定を追加（ミント/ブルー/オレンジ）。アイコン・円弧・数字は白PNGを
  `tintColor` で `[CONFIGURATION.color.*]` に連動させています。
- ベゼルリング・目盛りリングの画像を追加。
- 「bpm」「km」「kcal」のラベル画像を追加（グレー、ティントなし）。
  なお km/kcal は固定ラベルのため、提供元のテキストに単位が含まれる場合は
  重複表示になる可能性があります（旧デザインはこの理由でラベル未採用）。
- 歩数の `[STEP_PERCENT]` 進捗アークと目標値サブテキストを廃止
  （デザインでは全円弧が静的装飾）。
- AOD は日付を含まず HH:MM のみの表示に変更（Figma の ambient デザイン準拠）。
- アラーム/距離/カロリーは従来どおり編集可能なコンプリケーションスロット
  （スロット10/11/12）で実装し、提供元選択・EMPTY時フォールバック
  （`未設定`/`--`）・タップ動作は維持しています。

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
      raw/watchface.xml         # ウォッチフェイス定義（450x450、テーマカラー設定含む）
      xml/watch_face_info.xml   # プレビュー・編集可能宣言
      drawable/                 # アイコン・円弧・リング・ラベル画像、
                                # Inter ビットマップフォントグリフ（font_interregular_* / font_interbold_*）、
                                # プレビュー画像
      values/strings.xml        # テーマカラー名など
```
