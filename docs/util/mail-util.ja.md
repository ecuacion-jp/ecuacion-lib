# MailUtil

`MailUtil`（`jp.ecuacion.lib.core.util.MailUtil`）はメール送信のユーティリティクラスです。

---

## メソッド一覧

| メソッド | 説明 |
| --- | --- |
| `sendErrorMail(throwable)` | システムエラー発生時に管理者へ通知メールを送信 |
| `sendErrorMail(throwable, message)` | 追加メッセージ付きでエラーメールを送信 |
| `sendWarnMail(content, mailToList)` | 警告メールを送信 |
| `sendTextMail(toList, ccList, title, content)` | テキスト形式メールを送信 |
| `sendHtmlMail(toList, ccList, title, content)` | HTML 形式メールを送信 |

```java
// エラーメール（送信先は application.properties で設定）
MailUtil.sendErrorMail(throwable);
MailUtil.sendErrorMail(throwable, "バッチ処理中にエラーが発生しました");

// 任意の宛先にテキスト・HTML メールを送信
MailUtil.sendTextMail(List.of("admin@example.com"), null, "件名", "本文");
MailUtil.sendHtmlMail(null, List.of("cc@example.com"), "件名", "<b>本文</b>");
```

---

## application.properties の設定

プレフィックスはすべて `jp.ecuacion.lib.core.mail.` です。以下の表ではプレフィックスを省略します。

### 必須

| キー（プレフィックス省略） | 説明 | 例 |
| --- | --- | --- |
| `smtp.server` | SMTP サーバーのホスト名 ※1 | `smtp.gmail.com` |
| `smtp.port` | SMTP ポート番号 | `465`、`587` |
| `smtp.sender` | 送信元メールアドレス | `noreply@example.com` |
| `smtp.password` | SMTP パスワード（Gmail の場合はアプリパスワード） | `xxxx xxxx xxxx xxxx` |
| `smtp.authentication` | SMTP 認証の有無（`true` / `false`） | `true` |

※1 `MailUtil` は、サーバーが提示する TLS 証明書が接続先のホスト名と一致するかを常に検証します（`mail.smtp.ssl.checkserveridentity`。この検証を無効化する設定はありません）。自ドメイン（例: `mail.example.com`）専用の TLS 証明書が存在しない場合——共有レンタルサーバーでよくある、
メールサーバーがホスティング事業者側の共有ドメイン用の証明書を提示するケース——自ドメインのホスト名で接続しようとするとエラーになります。その場合は、自ドメインではなく、実際に証明書が発行されているオリジナルのホスト名を `smtp.server` に設定してください（ホスティング事業者に確認するか、
`openssl s_client -starttls smtp -connect <host>:587` で証明書の CN を確認できます）。

### 任意

| キー（プレフィックス省略） | 説明 | デフォルト |
| --- | --- | --- |
| `smtp.ssl-enabled` | ポート465での SSL/TLS 有効化（`true`）、またはポート587での STARTTLS 使用（`false`） | `false` |
| `smtp.starttls-required` | `smtp.ssl-enabled=false`（ポート587での STARTTLS）の場合のみ有効。`true` の場合、サーバーが STARTTLS に対応していないと平文にフォールバックせず接続自体を失敗させる。**`false` に設定するとセキュリティリスクがある**: サーバーが STARTTLS に対応していない場合、SMTP 認証（パスワードを含む）が平文で送信されてしまう。STARTTLS に対応していないことが分かっているサーバー（ローカルのテスト用リレーなど）向けにのみ `false` を設定し、本番環境では使用しないこと。 | `true` |
| `smtp.bounce-address` | バウンスメールの受信アドレス | 未設定（バウンス設定なし） |
| `debug` | Jakarta Mail のデバッグログ出力（`true` / `false`）。SMTP プロトコルのやり取り全体（認証時に送信される Base64 エンコード済みの認証情報を含む）をログ出力するため、本番環境では有効化しないこと。 | `false` |

### sendErrorMail / sendWarnMail 使用時に必須

| キー（プレフィックス省略） | 説明 | 例 |
| --- | --- | --- |
| `title-prefix` | メール件名の先頭に付与する文字列 | `[MyApp: staging]` |
| `address-csv-on-system-error` | `sendErrorMail` の送信先（カンマ区切りで複数可）※2 | `admin@example.com,ops@example.com` |

※2 `sendWarnMail` / `sendTextMail` / `sendHtmlMail` では送信先を引数で指定するため不要です。

---

## 設定例

```properties
# --- SMTP サーバー接続（必須）---
jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
jp.ecuacion.lib.core.mail.smtp.port=465
jp.ecuacion.lib.core.mail.smtp.sender=noreply@example.com
jp.ecuacion.lib.core.mail.smtp.password=xxxx xxxx xxxx xxxx
jp.ecuacion.lib.core.mail.smtp.authentication=true

# --- SSL（任意、ポート 465 使用時は true を推奨）---
jp.ecuacion.lib.core.mail.smtp.ssl-enabled=true

# --- sendErrorMail / sendWarnMail 使用時 ---
jp.ecuacion.lib.core.mail.title-prefix=[MyApp: staging]
jp.ecuacion.lib.core.mail.address-csv-on-system-error=admin@example.com
```

ポート 587 + STARTTLS の場合（`smtp.starttls-required` は明示指定不要。デフォルトの `true`
のままで、サーバーが STARTTLS に対応していなければ平文にフォールバックせず接続を失敗させる）：

```properties
jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
jp.ecuacion.lib.core.mail.smtp.port=587
jp.ecuacion.lib.core.mail.smtp.sender=noreply@example.com
jp.ecuacion.lib.core.mail.smtp.password=xxxx xxxx xxxx xxxx
jp.ecuacion.lib.core.mail.smtp.authentication=true
jp.ecuacion.lib.core.mail.smtp.ssl-enabled=false
```
