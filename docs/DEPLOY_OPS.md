# 고추밭 운영 배포 가이드 (Claude/개발자용)

> 사용자가 `DEPLOY_GABIA.md` 절차를 끝내고 §10 정보를 넘겨준 다음에 실행할 단계.
> 가비아 g클라우드 VM (Ubuntu 22.04 LTS 가정) + MySQL + Spring Boot + Vue 정적 빌드 + nginx.

---

## 0. 사용자에게서 받아야 할 정보

`DEPLOY_GABIA.md §10`이 그대로 넘어와야 한다. 빈 칸 있으면 진행 X.

```
공인 IP             : ___.___.___.___
SSH 사용자/비번      : root / ******
도메인              : example.com
DB 호스트/사용자/비번: localhost / gochubat / ******
네이버 client_id    : ******
네이버 client_secret: ******
```

---

## 1. SSH 접속 + 시스템 패키지

```bash
ssh root@<공인 IP>
apt update && apt -y upgrade
apt -y install openjdk-21-jdk mysql-server nginx ufw certbot python3-certbot-nginx
java -version       # openjdk 21 확인
mysql --version     # 8.x
nginx -v            # 1.18+
```

`ufw` 켜져 있으면 `ufw allow 22/tcp 80/tcp 443/tcp` 후 `ufw enable`.

---

## 2. MySQL 초기 설정

```bash
mysql_secure_installation     # root 비번 + anonymous 제거 + remote root 거부
mysql -u root -p
```

```sql
CREATE DATABASE gochubat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gochubat'@'localhost' IDENTIFIED BY '<DB 비번>';
GRANT ALL PRIVILEGES ON gochubat.* TO 'gochubat'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

스키마는 첫 부팅 시 JPA `ddl-auto: update`가 자동 생성 (`application-prod.yml`에서 설정).

---

## 3. 백엔드 — application-prod.yml + bootJar

로컬에서:

```bash
cd backend
./gradlew clean bootJar
# build/libs/gochubat-0.0.1-SNAPSHOT.jar 생성됨
```

배포 디렉토리 준비 (서버):

```bash
mkdir -p /opt/gochubat/{config,uploads,logs}
chown -R root:root /opt/gochubat
```

`application-prod.yml` 작성 (서버 `/opt/gochubat/config/application-prod.yml`, 권한 600):

```yaml
spring:
  profiles:
    default: prod
  datasource:
    url: jdbc:mysql://localhost:3306/gochubat?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: gochubat
    password: <DB 비번>
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update      # 첫 부팅 후 validate로 변경 권장
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

naver:
  client-id: <네이버 client_id>
  client-secret: <네이버 client_secret>
  callback-url: https://<도메인>/api/auth/naver/callback
  success-redirect-url: https://<도메인>/

jwt:
  secret: <openssl rand -base64 64로 생성한 64자+ 랜덤>
  access-expiration: 1800000
  refresh-expiration: 1209600000

gochubat:
  cors:
    allowed-origins: https://<도메인>
  upload:
    dir: /opt/gochubat/uploads
    max-size-bytes: 10485760
    allowed-mime-types:
      - image/jpeg
      - image/png
      - image/webp
      - image/gif
    public-base-url: /uploads
  point:
    post-created: 10
    comment-created: 2
    like-received: 1
    pepper-threshold: 100
    corn-threshold: 500

logging:
  file:
    path: /opt/gochubat/logs
  level:
    com.gochubat: INFO
```

JAR 업로드:

```bash
scp build/libs/gochubat-0.0.1-SNAPSHOT.jar root@<공인 IP>:/opt/gochubat/app.jar
```

systemd 서비스 (`/etc/systemd/system/gochubat.service`):

```ini
[Unit]
Description=Gochubat Spring Boot
After=network.target mysql.service

[Service]
User=root
WorkingDirectory=/opt/gochubat
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod -Dspring.config.additional-location=/opt/gochubat/config/ /opt/gochubat/app.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
StandardOutput=append:/opt/gochubat/logs/stdout.log
StandardError=append:/opt/gochubat/logs/stderr.log

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable gochubat
systemctl start gochubat
systemctl status gochubat       # active (running) 확인
curl http://127.0.0.1:8080/actuator/health   # {"status":"UP"}
```

---

## 4. 프론트엔드 — 빌드 + 정적 배포

로컬 `frontend/.env.production` 작성:

```
VITE_API_URL=https://<도메인>
VITE_USE_MOCK=false
VITE_NAVER_CLIENT_ID=<네이버 client_id>
```

빌드:

```bash
cd frontend
npm ci
npm run build
# dist/ 생성됨
```

서버에 업로드:

```bash
ssh root@<공인 IP> "rm -rf /var/www/gochubat && mkdir -p /var/www/gochubat"
scp -r dist/* root@<공인 IP>:/var/www/gochubat/
```

---

## 5. nginx 설정

`/etc/nginx/sites-available/gochubat`:

```nginx
server {
    listen 80;
    server_name <도메인>;

    # SPA static
    root /var/www/gochubat;
    index index.html;

    # 업로드 정적 서빙 (Spring 보조용; 운영에선 nginx로 직접 처리)
    location /uploads/ {
        alias /opt/gochubat/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # API 프록시
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 12M;
    }

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

```bash
ln -s /etc/nginx/sites-available/gochubat /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t                      # 문법 검증
systemctl reload nginx
```

http://<도메인> 접속해서 화면 뜨는지 확인.

---

## 6. HTTPS — Let's Encrypt

```bash
certbot --nginx -d <도메인> --non-interactive --agree-tos -m <연락 이메일>
```

certbot이 nginx config를 자동 수정해서 80 → 443 리다이렉트 + SSL 추가. 갱신은 cron으로 자동 (`certbot renew --dry-run`으로 확인).

`application-prod.yml`의 `naver.callback-url` / `success-redirect-url` / `gochubat.cors.allowed-origins`가 모두 `https://`로 되어 있어야 쿠키 SameSite=Lax + Secure 동작.

---

## 7. 첫 OWNER 계정 등록

1. 사용자가 https://<도메인>/login 으로 가서 네이버 로그인 (계정 생성, default tier=SEED)
2. SSH로 들어가서 MySQL 접속:

```sql
USE gochubat;
SELECT id, naver_id, nickname, tier FROM users;
UPDATE users SET tier='OWNER' WHERE id=<위에서 본 본인 id>;
```

3. 다시 로그아웃/재로그인하면 JWT에 OWNER role 박힘 → /admin 접근 가능.

---

## 8. 첫 부팅 후 안전 모드

`application-prod.yml`의 `ddl-auto: update` → `validate`로 변경 후 `systemctl restart gochubat`. 운영 중 스키마 자동 변경 차단.

---

## 9. 트러블슈팅

| 증상 | 확인 |
|---|---|
| 502 Bad Gateway | `systemctl status gochubat` + `tail /opt/gochubat/logs/stderr.log` |
| CORS 차단 | `application-prod.yml` `gochubat.cors.allowed-origins` 가 `https://<도메인>` 정확한지 |
| 네이버 로그인 후 callback 404 | 네이버 개발자센터 callback URL이 `https://<도메인>/api/auth/naver/callback` 인지 |
| 이미지 업로드 후 broken | `/opt/gochubat/uploads` 권한 (`chown -R root:root`) + nginx `alias` 슬래시 정확한지 |
| 로그인 후 즉시 로그아웃 | 쿠키 Secure 플래그 — HTTPS 미설정 시 브라우저가 거절 |
| OOM | systemd `Environment=JAVA_TOOL_OPTIONS=-Xmx512m` 추가 (가비아 g클라우드 1GB 인스턴스 가정) |

---

## 10. 헬스체크

배포 직후 확인:

```bash
curl -i https://<도메인>/actuator/health      # {"status":"UP"}
curl -i https://<도메인>/api/notices          # {"data":[],"total":0}
curl -i https://<도메인>/api/cafe/config      # 기본 설정 객체
curl -i https://<도메인>/                     # index.html
```

브라우저 개발자도구 Network 탭에서:
- 첫 진입 → `/api/me/admin` 200 + `Set-Cookie` 없음 (anon)
- 네이버 로그인 후 → `/api/me` 200 + 쿠키 `access_token=...; HttpOnly; Secure; SameSite=Lax`
- /admin (OWNER 등급으로) → `/api/admin/dashboard` 200, 비-OWNER → 403

---

## 11. 롤백

새 jar이 깨졌을 때:

```bash
systemctl stop gochubat
cp /opt/gochubat/app.jar.bak /opt/gochubat/app.jar
systemctl start gochubat
```

배포 전 항상 `cp /opt/gochubat/app.jar /opt/gochubat/app.jar.bak`.
