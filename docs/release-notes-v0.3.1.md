# RC-PostFlow v0.3.1

> 公開日: 2026-09-22
>
> バージョン: v0.3.1
>
> Gitタグ: v0.3.1（作成予定）
>
> ステータス: 公開準備中

## v0.3.1の概要

RC-PostFlowは、SNS投稿の登録、予約、配信結果の確認を一元管理し、SNS運用にかかる作業時間を減らすための投稿管理アプリケーションです。

RC-PostFlow v0.3.1は、Threadsへの予約投稿を中心としたMVPです。単一の管理者が、投稿の準備から配信結果の確認までを一つの画面で行えることを目的としています。

## v0.3.1の変更点

- Windows PowerShellおよびVS Code統合ターミナルで、端末のコードページと異なるUTF-8を標準出力・標準エラーへ強制していた設定を削除しました。
- Javaソースのコンパイル文字コードをUTF-8へ明示しました。
- ファイルログの文字コードをUTF-8へ明示しました。
- Windowsで日本語ログが文字化けする場合の確認・対処手順をREADMEへ追加しました。

既存の`v0.3.0`タグは上記修正前の履歴として変更・削除・移動せず保持します。

## 主な機能

- Threads投稿の登録、一覧表示、編集、削除
- 日時を指定した予約投稿
- 投稿タイトル、本文、任意URLの入力とバリデーション
- キーワード、チャネル、投稿状態による一覧の絞り込み
- 投稿状態とKPI（投稿予約、投稿済み、投稿失敗）の表示
- 単一管理者によるフォームログイン、CSRF保護、ログアウト
- SQLiteを使用したデータ永続化
- FlywayによるデータベースMigration

v0.3.1の正式サポート対象データベースはSQLiteのみです。PostgreSQL向けのコード、設定、Flyway Migrationは残存していますが、v0.3.1では未検証であり、正式サポート対象外です。

投稿状態は次のように表示されます。

| 内部状態 | 画面表示 | 説明 |
| --- | --- | --- |
| `PENDING` | 予約済み | Schedulerによる処理待ち |
| `SCHEDULED` | 投稿処理中 | Threadsへの投稿処理を開始済み |
| `POSTED` | 投稿済み | 投稿成功の応答を受け、結果を保存済み |
| `ERROR` | 投稿失敗 | 投稿処理中にエラーが発生 |

投稿予約KPIは`PENDING`のみを集計します。KPIは全投稿を対象とし、画面の一覧フィルターには追従しません。

## 動作環境と検証範囲

| 項目 | バージョン／構成 | 検証範囲 |
| --- | --- | --- |
| Java | 25 | Gradle Toolchainで指定。自動テストはEclipse Temurin 25.0.3 LTSで実行 |
| Spring Boot | 3.5.16 | 現行ビルド設定と自動テストで確認 |
| Gradle Wrapper | 8.14.5 | Windows環境で自動テストを実行 |
| SQLite JDBC | 3.49.1.0 | 通常プロファイルの設定と、テスト用インメモリDBで確認 |
| PostgreSQL JDBC | 42.7.11 | コードと設定が残存。v0.3.1では実DB接続未検証、正式サポート対象外 |
| UI | Thymeleaf、HTML、CSS、JavaScript | 自動テストおよび撮影専用環境で確認 |

v0.3.1の正式サポート対象はSQLiteのみです。PostgreSQL向けのSpringプロファイルとFlyway Migrationは残存していますが、実PostgreSQL環境での接続、Migration、CRUD、Scheduler、予約投稿処理は未検証です。PostgreSQLに関する情報は参考扱いであり、動作を保証するものではありません。

## テストと実投稿の確認状況

### 自動テスト

2026年9月22日に、v0.3.1を対象としてWindows 11、Eclipse Temurin 25.0.3 LTS、Gradle Wrapper 8.14.5で自動テストを実行し、57件すべて成功しました。

- 成功: 57件
- 失敗: 0件
- エラー: 0件
- スキップ: 0件

テストには、CRUD、Validation、Security、Scheduler、Threadsクライアント、投稿状態表示、KPI集計が含まれます。SQLiteのインメモリDBとモック／テスト用HTTP応答を使用しており、Threadsサービスへの実投稿と実PostgreSQL接続は含まれません。

### Threads実投稿の確認

2026年9月22日に、現行v0.3.1コードのSQLite環境で予約投稿を確認しました。

- Access Token未設定の状態で予約日時を迎えた投稿1件が失敗し、`ERROR`へ遷移しました。
- 有効なAccess Tokenを設定した後の予約投稿は成功しました。
- RC-PostFlow側で投稿状態が`POSTED`へ遷移しました。
- Threads側で実投稿を確認しました。

実Token、投稿内容、アカウント情報は記録していません。詳細は[サニタイズ済みの検証履歴](verification-history.md)を参照してください。

### Threads実投稿の過去確認

過去の`v0.2.0-pre1`プレ運用では、2026年7月5日にThreads投稿成功、2026年7月6日に予約投稿成功とサービス継続稼働が記録されています。詳細は[サニタイズ済みの過去検証履歴](verification-history.md)を参照してください。

これは過去バージョンに対する手動確認履歴であり、上記の現行v0.3.1確認とは区別しています。導入環境ごとに、有効なAccess Tokenを使用して確認してください。

## 初回導入

必要な環境変数、Windows／LinuxのSQLite起動コマンド、ログイン、最初の投稿登録については、[READMEの初回セットアップ](../README.md#初回セットアップ)を参照してください。PostgreSQL向けの残存実装に関する情報は、READMEの参考情報に分離しています。

Threadsへ実投稿する場合は、アプリケーション起動前に有効な`THREADS_ACCESS_TOKEN`を設定してください。Token未設定でも起動はできますが、予約日時を迎えた投稿は設定エラーとなり、`ERROR`へ遷移します。

外部投稿を行わず画面操作だけを確認する場合は、通常DBから分離され、Schedulerを生成しない[撮影専用環境](screenshot-environment.md)を利用してください。

## 既知の制約と注意事項

- 単一管理者、単一アプリケーションインスタンスでの運用を前提とします。
- Schedulerは60秒の固定delayで動作します。
- 投稿失敗時の自動リトライはありません。
- `SCHEDULED`保存後にプロセスが停止すると、その状態に滞留し、自動では再処理されません。
- Threads APIへの送信後に応答受信やDB更新が失敗すると、画面が`ERROR`または`SCHEDULED`でもThreads側には投稿済みの可能性があります。
- 外部投稿IDを保存していないため、重複投稿を完全には防止できません。再登録前にThreads側の投稿結果を確認してください。
- Access Tokenの取得、自動更新、失効通知には対応していません。
- 複数インスタンス間の排他制御や分散ロックは実装していません。

v0.3.1では、Threads以外のSNS投稿、連続スレッド投稿、画像・動画添付、即時投稿、独立ダッシュボード、複数ユーザー管理には対応していません。

## ライセンスとブランド資産

RC-PostFlowのソースコードは、[Apache License, Version 2.0](../LICENSE)に基づいて公開します。

RC-PostFlow／RC-SIP／R-Companyのロゴ・アイコンなどのブランド画像は、Apache License 2.0の適用対象には含まれません。公式配布版以外での使用や再配布に関する条件は、[RC-PostFlow ブランド利用ガイドライン](../BRANDING.md)を確認してください。

ブランド画像の対象ファイルと権利帰属は、公開前に最終確認が必要です。

## バージョンと公開状況

今回の公開版はRC-PostFlow v0.3.1です。

- 公開向け名称: `RC-PostFlow v0.3.1`
- `build.gradle`のバージョン: `0.3.1`
- 過去の開発履歴で使用したタグ名: `v0.2.0`、`v0.2.0-pre1`（初回OSS公開用の履歴には引き継がない）
- `v0.3.0`タグ: 修正前の既存タグとして不変のまま保持
- `v0.3.1`タグ: 未作成（作成予定）
- 公開用のroot commit: 作成済み
- Git remote: `https://github.com/r-company-net/rc-postflow.git`
- GitHub Release: ドラフト（未公開）
- 公開Repository: `https://github.com/r-company-net/rc-postflow`

公開前に`build.gradle`、作成予定の`v0.3.1`タグ、GitHub Releaseのタイトルと配布物の表記が一致することを最終確認してください。本ドラフトの更新に伴うタグ作成、remote設定、push、GitHub Release公開は行っていません。

## フィードバック

公開Repositoryは[https://github.com/r-company-net/rc-postflow](https://github.com/r-company-net/rc-postflow)です。バグ報告、改善提案、フィードバックは[GitHub Issues](https://github.com/r-company-net/rc-postflow/issues)を利用してください。

## 公開前チェック項目

- ビルドバージョン、作成予定の`v0.3.1`タグ、GitHub Release表記が一致することを最終確認する
- `build/libs/rc-postflow-0.3.1.jar`を生成し、自動テスト結果と配布物名を確認する
- README、Release Note、検証履歴、画像の相対リンク切れがないことを確認する
- 将来PostgreSQLを正式サポート対象にする場合は、実DBで接続、Migration、CRUD、Scheduler、予約投稿処理を検証する
- ブランド資産の対象ファイル、権利帰属、再配布条件を最終確認する
