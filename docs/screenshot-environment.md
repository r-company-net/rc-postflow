# OSS公開用スクリーンショット環境

撮影環境は通常のSQLite DBとは別の `data/rc-postflow-screenshot.db` を使用する。
このパスは既存の `.gitignore` の `/data/*.db` に一致するため、DBはGit追跡対象にならない。

## 安全対策

- `PostScheduler` は `screenshot` プロファイルを明示的に除外しているため、撮影環境では生成されない。
- Threads APIの接続先は到達不能なローカルアドレス `http://127.0.0.1:9` に固定する。
- ThreadsのApp ID、Secret、Access Tokenはすべて空にする。
- 管理者Credentialはファイルに保存せず、起動プロセスの環境変数でのみ渡す。
- 通常プロファイルではSchedulerの既定値が有効なため、v0.3.1の通常動作は変わらない。

## 初回起動

PowerShellで管理者Credentialを現在のプロセスにだけ設定し、専用スクリプトを実行する。

```powershell
$env:RC_POSTFLOW_ADMIN_USERNAME = Read-Host '撮影用ユーザー名'
$env:RC_POSTFLOW_ADMIN_PASSWORD = Read-Host '撮影用パスワード'
.\scripts\start-screenshot.ps1
```

ブラウザで `http://localhost:8080` を開く。初回起動時にFlywayが専用DBを作成し、
撮影日を基準とした架空データ8件（予約4件、投稿済み3件、投稿失敗1件）を投入する。

## 再生成

サンプルを初期状態へ戻す必要がある場合に限り、アプリケーション停止後に
`data/rc-postflow-screenshot.db` を削除して再起動する。通常DBの
`data/rc-postflow.db` は削除・変更しない。
