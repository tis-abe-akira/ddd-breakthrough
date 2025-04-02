# シンジケートローン管理システム

## 概要
このシステムは、シンジケートローンの管理を行うためのバックエンドAPIを提供します。
主な機能として、ファシリティの管理、投資の管理、ドローダウン、各種支払い（手数料、利息、元本）の管理を行います。

## 技術スタック
- Java 17
- Spring Boot 3.2
- Maven 3
- H2 Database (開発用インメモリデータベース)

## セットアップと起動方法

```bash
cd backend
./mvnw spring-boot:run
```

## 初期データの投入

動作確認のために、まず以下の順序でマスターデータを登録します：

### 1. 借り手の登録
```bash
curl -X POST http://localhost:8080/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Intel Corporation",
    "creditRating": "A+",
    "financialStatements": "test",
    "contactInformation": "test",
    "companyType": "CORPORATION",
    "industry": "Technology"
  }'
```

### 2. 投資家の登録
```bash
# 投資家1の登録 (investmentCapacity: 10,000,000)
curl -X POST http://localhost:8080/api/investors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MUFG Bank",
    "type": "BANK",
    "investmentCapacity": 10000000,
    "currentInvestments": 0
  }'

# 投資家2の登録 (investmentCapacity = 20,000,000)
curl -X POST http://localhost:8080/api/investors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mizuho Bank",
    "type": "BANK",
    "investmentCapacity": 20000000,
    "currentInvestments": 0
  }'
```

## 動作確認手順

以下の順序でAPIを呼び出すことで、基本的な機能を確認できます。

### 1. シンジケートの作成 (totalCommitment: 5,000,000)
```bash
curl -X POST http://localhost:8080/api/syndicates \
  -H "Content-Type: application/json" \
  -d '{
    "leadBankId": 1,
    "memberIds": [1, 2],
    "totalCommitment": 5000000
  }'
```

### 2. シェアパイの作成
```bash
curl -X POST http://localhost:8080/api/share-pies \
  -H "Content-Type: application/json" \
  -d '{
    "shares": {
      "1": 30,
      "2": 70
    }
  }'
```

### 3. ファシリティの作成 (totalAmount: 5,000,000)
```bash
curl -X POST http://localhost:8080/api/facilities \
  -H "Content-Type: application/json" \
  -d '{
    "totalAmount": 5000000,
    "availableAmount": 5000000,
    "startDate": "2025-01-27",
    "term": 60,
    "interestRate": 2.5,
    "syndicateId": 1,
    "sharePieId": 1,
    "borrowerId": 1
  }'
```

### 4. ファシリティ投資の登録と実行 (investmentAmount: 5,000,000)
TODO: sharePieの登録は2で済んでいると思うので、ここではIDを指定するべきではないか？ -> relatedPositionIdから推移的にSharePieにアクセス可能！

```bash
curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "investorId": 1,
    "date": "2025-01-28T10:00:00",
    "processedDate": "2025-01-29T10:00:00",
    "relatedPositionId": 1
  }'

curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "investorId": 2,
    "date": "2025-01-29T10:00:00",
    "processedDate": "2025-01-30T10:00:00",
    "relatedPositionId": 1
  }'
```

### 5. ドローダウンの登録と実行
```bash
curl -X POST http://localhost:8080/api/drawdowns \
  -H "Content-Type: application/json" \
  -d '{
    "relatedFacilityId": 1,
    "drawdownAmount": 2000000,
    "date": "2025-01-31T14:00:00",
    "processedDate": "2025-01-31T14:00:00",
    "relatedPositionId": 1,
    "amountPie": {
      "amounts": {
        "1": 400000,
        "2": 1600000
      }
    }
  }'
```

```bash
# ドローダウンの実行
curl -X PUT http://localhost:8080/api/drawdowns/3/execute
```

### 6. 手数料支払いの登録と実行
```bash
# 手数料支払いの登録
curl -X POST http://localhost:8080/api/fee-payments \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": 1,
    "feeType": "COMMITMENT_FEE",
    "paymentAmount": 20000,
    "date": "2025-01-31T15:00:00",
    "relatedPositionId": 1
  }'

# 手数料支払いの実行
curl -X PUT http://localhost:8080/api/fee-payments/4/execute
```

### 7. 利息支払いの登録と実行
```bash
# 利息支払いの登録
curl -X POST http://localhost:8080/api/interest-payments \
  -H "Content-Type: application/json" \
  -d '{
    "loanId": 2,
    "date": "2025-06-15T10:00:00"
  }'

# 利息支払いの実行
curl -X PUT http://localhost:8080/api/interest-payments/5/execute
```

### 8. 元本返済の登録と実行
```bash
# 元本返済の登録
curl -X POST http://localhost:8080/api/principal-payments \
  -H "Content-Type: application/json" \
  -d '{
    "loanId": 1,
    "paymentAmount": 10000000,
    "date": "2024-02-20T11:00:00",
    "amountPie": {
      "amounts": [
        {"investorId": 1, "amount": 5000000},
        {"investorId": 2, "amount": 5000000}
      ]
    }
  }'

# 元本返済の実行
curl -X PUT http://localhost:8080/api/principal-payments/1/execute
```

### データの確認
各APIの実行結果は、以下のGETリクエストで確認できます：

```bash
# ファシリティ投資一覧
curl http://localhost:8080/api/facility-investments

# ドローダウン一覧
curl http://localhost:8080/api/drawdowns

# 手数料支払い一覧
curl http://localhost:8080/api/fee-payments

# 利息支払い一覧
curl http://localhost:8080/api/interest-payments

# 元本返済一覧
curl http://localhost:8080/api/principal-payments
```

## データベースの参照方法

本システムはH2データベースをインメモリモードで使用しています。
データベースの内容は以下の方法で確認できます：

### 1. H2 Consoleへのアクセス
アプリケーション起動後、ブラウザで以下のURLにアクセスします：
```
http://localhost:8080/h2-console
```

### 2. 接続設定
H2 Console画面で以下の設定を入力します：
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: (空白)

### 3. 主要なテーブルと参照用SQL

```sql
-- マスターデータの確認
SELECT * FROM borrower;
SELECT * FROM investor;

-- ファシリティの確認
SELECT * FROM facility;

-- ファシリティ投資の確認
SELECT * FROM facility_investment;

-- ドローダウンの確認
SELECT * FROM drawdown;

-- 手数料支払いの確認
SELECT * FROM fee_payment;

-- 利息支払いの確認
SELECT * FROM interest_payment;

-- 元本返済の確認
SELECT * FROM principal_payment;

-- シェアの確認
SELECT * FROM share_pie;
SELECT * FROM share_pie_shares;

-- 金額配分の確認
SELECT * FROM amount_pie;
SELECT * FROM amount_pie_amounts;

-- 取引の状態確認
SELECT t.id, t.type, t.date, t.status, t.processed_date
FROM transaction t
ORDER BY t.date;
```

## 注意点
1. IDは実際のデータベースの状態に応じて適切な値に変更してください
2. 日付は現在の日付に合わせて調整してください
3. 金額は実際のビジネスルールに従って設定してください
4. エラーが発生した場合は、レスポンスのエラーメッセージを確認してください
