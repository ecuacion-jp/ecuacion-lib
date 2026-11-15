# MailUtil

`MailUtil` (`jp.ecuacion.lib.core.util.MailUtil`) is a utility class for sending emails.

---

## Method List

| Method | Description |
| --- | --- |
| `sendErrorMail(throwable)` | Sends a notification email to the administrator when a system error occurs |
| `sendErrorMail(throwable, message)` | Sends an error email with an additional message |
| `sendWarnMail(content, mailToList)` | Sends a warning email |
| `sendTextMail(toList, ccList, title, content)` | Sends an email in plain text format |
| `sendHtmlMail(toList, ccList, title, content)` | Sends an email in HTML format |

```java
// Error email (recipient configured in application.properties)
MailUtil.sendErrorMail(throwable);
MailUtil.sendErrorMail(throwable, "An error occurred during batch processing");

// Send text/HTML email to arbitrary recipients
MailUtil.sendTextMail(List.of("admin@example.com"), null, "Subject", "Body");
MailUtil.sendHtmlMail(null, List.of("cc@example.com"), "Subject", "<b>Body</b>");
```

---

## application.properties Configuration

All keys have the prefix `jp.ecuacion.lib.core.mail.`. The prefix is omitted in the table below.

### Required

| Key (prefix omitted) | Description | Example |
| --- | --- | --- |
| `smtp.server` | SMTP server hostname *1 | `smtp.gmail.com` |
| `smtp.port` | SMTP port number | `465`, `587` |
| `smtp.sender` | Sender email address | `noreply@example.com` |
| `smtp.password` | SMTP password (for Gmail, use an app password) | `xxxx xxxx xxxx xxxx` |
| `smtp.authentication` | Whether SMTP authentication is used (`true` / `false`) | `true` |

*1 `MailUtil` always verifies that the TLS certificate presented by the server matches
the hostname it connected to (`mail.smtp.ssl.checkserveridentity`; there is no setting
to disable this check). If your own domain (e.g. `mail.example.com`) doesn't have its
own TLS certificate — common on shared hosting, where the mail server presents a
certificate for the hosting provider's shared domain instead — connecting with your own
domain name will fail. In that case, set `smtp.server` to the original hostname the
certificate was actually issued for (check with your hosting provider, e.g. via
`openssl s_client -starttls smtp -connect <host>:587`), not your own domain.

### Optional

| Key (prefix omitted) | Description | Default |
| --- | --- | --- |
| `smtp.ssl-enabled` | Enable SSL/TLS on port 465 (`true`), or use STARTTLS on port 587 (`false`) | `false` |
| `smtp.starttls-required` | Applies only when `smtp.ssl-enabled=false` (STARTTLS on port 587). `true` fails the connection rather than falling back to plaintext when the server doesn't support STARTTLS. **Setting this to `false` is a security risk**: SMTP authentication (including the password) would then be sent in the clear whenever the server doesn't offer STARTTLS. Only set it to `false` for a server known not to support STARTTLS (e.g. a local test relay), never in production. | `true` |
| `smtp.bounce-address` | Address to receive bounce emails | Not set (no bounce configuration) |
| `debug` | Enable Jakarta Mail debug logging (`true` / `false`). Logs the full SMTP protocol exchange, including the Base64-encoded credentials sent during authentication — do not enable in production. | `false` |

### Required When Using sendErrorMail / sendWarnMail

| Key (prefix omitted) | Description | Example |
| --- | --- | --- |
| `title-prefix` | String prepended to the email subject | `[MyApp: staging]` |
| `address-csv-on-system-error` | Recipients for `sendErrorMail` (comma-separated for multiple) *2 | `admin@example.com,ops@example.com` |

*2 Not needed for `sendWarnMail` / `sendTextMail` / `sendHtmlMail`, as recipients are specified as arguments.

---

## Configuration Example

```properties
# --- SMTP server connection (required) ---
jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
jp.ecuacion.lib.core.mail.smtp.port=465
jp.ecuacion.lib.core.mail.smtp.sender=noreply@example.com
jp.ecuacion.lib.core.mail.smtp.password=xxxx xxxx xxxx xxxx
jp.ecuacion.lib.core.mail.smtp.authentication=true

# --- SSL (optional; recommended to set true when using port 465) ---
jp.ecuacion.lib.core.mail.smtp.ssl-enabled=true

# --- When using sendErrorMail / sendWarnMail ---
jp.ecuacion.lib.core.mail.title-prefix=[MyApp: staging]
jp.ecuacion.lib.core.mail.address-csv-on-system-error=admin@example.com
```

Port 587 with STARTTLS (`smtp.starttls-required` doesn't need to be set explicitly;
it defaults to `true`, which fails the connection instead of silently falling back to
plaintext if the server doesn't support STARTTLS):

```properties
jp.ecuacion.lib.core.mail.smtp.server=smtp.gmail.com
jp.ecuacion.lib.core.mail.smtp.port=587
jp.ecuacion.lib.core.mail.smtp.sender=noreply@example.com
jp.ecuacion.lib.core.mail.smtp.password=xxxx xxxx xxxx xxxx
jp.ecuacion.lib.core.mail.smtp.authentication=true
jp.ecuacion.lib.core.mail.smtp.ssl-enabled=false
```
