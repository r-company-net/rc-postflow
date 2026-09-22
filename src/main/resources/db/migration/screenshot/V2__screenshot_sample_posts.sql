INSERT INTO posts (title, body, url, scheduled_at, channels, status) VALUES
(
    '秋のプロダクトアップデート予告',
    '架空の新機能を紹介するサンプル投稿です。より使いやすくなったダッシュボードを近日公開予定です。',
    'https://example.invalid/product-update',
    datetime('now', 'localtime', '+1 day', 'start of day', '+10 hours'),
    'THREADS',
    'SCHEDULED'
),
(
    'オンラインセミナー開催のお知らせ',
    '架空のオンラインセミナー開催案内です。コンテンツ運用のヒントをわかりやすくご紹介します。',
    'https://example.invalid/webinar',
    datetime('now', 'localtime', '+2 days', 'start of day', '+14 hours'),
    'THREADS',
    'SCHEDULED'
),
(
    'チームインタビュー公開予告',
    '架空の開発チームインタビューを近日公開します。プロダクトづくりの舞台裏をお届けします。',
    'https://example.invalid/team-interview',
    datetime('now', 'localtime', '+3 days', 'start of day', '+18 hours'),
    'THREADS',
    'SCHEDULED'
),
(
    '月末ニュースレター配信予定',
    '架空の月末ニュースレターです。今月のアップデートと来月のイベント情報をまとめてお届けします。',
    'https://example.invalid/newsletter',
    datetime('now', 'localtime', '+7 days', 'start of day', '+12 hours'),
    'THREADS',
    'SCHEDULED'
),
(
    'サービス活用ガイドを公開しました',
    '架空のサービス活用ガイドを公開しました。初めての方にもわかりやすい内容になっています。',
    'https://example.invalid/guide',
    datetime('now', 'localtime', '-1 day', 'start of day', '+11 hours'),
    'THREADS',
    'POSTED'
),
(
    'コミュニティイベント開催レポート',
    '架空のコミュニティイベントの様子をレポートします。ご参加いただいた皆さま、ありがとうございました。',
    'https://example.invalid/community-report',
    datetime('now', 'localtime', '-3 days', 'start of day', '+16 hours'),
    'THREADS',
    'POSTED'
),
(
    'UI改善アップデートのお知らせ',
    '架空のUI改善アップデートを公開しました。投稿管理画面がさらに見やすくなりました。',
    'https://example.invalid/ui-update',
    datetime('now', 'localtime', '-7 days', 'start of day', '+9 hours'),
    'THREADS',
    'POSTED'
),
(
    'キャンペーンページ更新のお知らせ',
    '架空のキャンペーン告知です。撮影用に投稿失敗状態を確認するためのサンプルデータです。',
    'https://example.invalid/campaign',
    datetime('now', 'localtime', '-2 hours'),
    'THREADS',
    'ERROR'
);
