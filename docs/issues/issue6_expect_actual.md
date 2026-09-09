# Issue #6: expect / actual による位置情報取得

## 概要
天気予報を表示するために、各プラットフォームの **位置情報 API** を使ってデバイスの現在地（緯度・経度）を取得します。

## この Issue で触れる概念
- **expect / actual**: `commonMain` で API を宣言し、各プラットフォームで実装を差し替える KMP の仕組み
- 補足: Issue #2 で使っている **`getMockServerBaseUrl()`** も expect / actual の例です（`MockServerUrl.kt`）。

## 前提
- Issue #2〜#5 が、**固定の緯度・経度** で動作していること。
- 本 Issue 完了後、ViewModel は「取得した現在地」で天気 API を呼ぶように変更します。

## 課題内容
- [ ] `commonMain` に位置情報を表すデータクラス `Location` と、位置情報を取得する `expect` 宣言を定義する。
- [ ] `androidMain` で `actual` 実装を行う（Android の位置情報 API を使用）。
- [ ] `iosMain` で `actual` 実装を行う（iOS Core Location を使用）。
- [ ] 取得した緯度・経度を ViewModel に渡し、天気 API の呼び出しに使うよう変更する。

## 要件
- インターフェース（または expect 宣言）は必ず `commonMain` に配置すること。
- **位置情報の権限**を考慮すること（権限がない場合のエラー表示や、権限リクエストの流れを簡易的にでもよいので実装する）。

## 権限設定のチェックリスト

### Android (`androidApp/src/main/AndroidManifest.xml`)
`AndroidManifest.xml` におおよそ次の権限を追加します（API レベルに応じて `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` を選択）。

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

実行時権限（ランタイムパーミッション）のリクエストは、`androidApp` の `MainActivity` または位置情報を取得するコードから行います。

### iOS (`iosApp/iosApp/Info.plist`)
次のキーを追加し、ユーザーに表示する説明文を設定します。

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>現在地の天気を表示するために位置情報を使用します</string>
```

## ヒント
- **Android**: Google Play services の `FusedLocationProviderClient` を使う場合、`composeApp/build.gradle.kts` の `androidMain.dependencies` にライブラリ追加が必要になることがあります。
- **iOS**: `CLLocationManager` を Kotlin から利用します。デリゲートのコールバックは非同期になるため、`suspend` 関数や `Flow` で `commonMain` に結果を渡すと扱いやすいです。

## 現在地であることの確認方法

Issue #2〜#5 では **東京付近の固定座標**（例: `35.68`, `139.76`）で天気を取っていました。  
Issue #6 の完了判定は、**シミュレータ／実機の位置を変えると、取得した緯度・経度と天気の内容が変わること** です（常に同じ数字のままなら、まだ固定値の可能性があります）。

### Mock サーバーで地域名が変わる（見やすい確認）

研修用 Mock サーバー（`/server`）は、渡した緯度・経度から **おおよその地域ラベル** を `location` に含めて返します（厳密な住所ではありません）。  
シミュレータの位置を変えて再取得すると、例えば `Tokyo area` → `Osaka area` のように **天気の地点名が変わる** ため、現在地連携の確認がしやすくなります。

| おおよその座標（例） | Mock が返す地域ラベル（例） |
| :--- | :--- |
| `35.68`, `139.76`（東京付近） | `Tokyo area` |
| `34.69`, `135.50`（大阪付近） | `Osaka area` |
| `43.06`, `141.35`（札幌付近） | `Sapporo area` |
| 上記以外 | `Other area` |

ブラウザで `http://localhost:8080/weather?lat=34.69&lon=135.50` を開き、`location` が `Osaka area (...)` になることも確認できます。  
実装の詳細は `server/.../Application.kt` の `resolveRegionLabel` を参照してください。

### 手順（推奨）

1. **実装前の値をメモする**  
   Issue #6 前は、表示やログがだいたい `35.68` / `139.76` 付近のまま変わらないことを確認しておく。

2. **アプリで位置情報の権限を許可する**  
   拒否した場合は `Error` 等になることも、別途確認する。

3. **シミュレータで仮の現在地を変える**

   | 環境 | 設定の例 |
   | :--- | :--- |
   | **Android Emulator** | ⋮ メニュー → **Location** → 地図上でピンを動かす、または緯度・経度を直接入力 |
   | **iOS Simulator** | メニュー **Features → Location** → **Custom Location…** などで座標を変更 |

   例: 一度 **大阪付近**（おおよそ `34.69`, `135.50`）、次に **札幌付近**（おおよそ `43.06`, `141.35`）に変える。

4. **アプリを再起動するか、天気の再取得を実行する**  
   位置情報を取り直し、ログまたは画面の **緯度・経度** を見る。

5. **次を確認する**

   | 確認項目 | 期待する結果 |
   | :--- | :--- |
   | 緯度・経度 | 手順 3 で設定した付近の値に **変わる**（固定の `35.68` / `139.76` のままではない） |
   | 天気の地点名など | Mock の `location` が **地域ラベル**（例: `Osaka area`）と緯度・経度付きで返る。座標を変えると **地域ラベルも変わる** こと |
   | 再設定 | 別の座標に変えたら、**再度** 緯度・経度と天気表示が変わる |

6. **（任意）実機**  
   実機では屋外・位置情報 ON で、移動または地図アプリとおおまかな一致を確認する。

> **ログの例**: `WeatherTraining: location=lat 34.69, lon 135.50` のあと、天気の `location` フィールドにも同じ付近の数値が出る、など。

### うまく判定できないとき

- 常に `35.68` / `139.76` → ViewModel や Repository に **固定値が残っていないか** 確認する。
- 座標が `0.0` / `null` → 権限未許可・取得失敗。権限フローと `actual` 実装を見直す。
- 座標だけ変わり天気が変わらない → API 呼び出しに **取得した緯度・経度を渡しているか** 確認する。

## 完了条件
- Android / iOS のシミュレータまたは実機で、**上記「現在地であることの確認方法」** のとおり、位置を変えると緯度・経度と天気の内容が変わること。
- 緯度・経度がログまたは画面に表示され、その座標で Mock API から天気が取得できていること。
