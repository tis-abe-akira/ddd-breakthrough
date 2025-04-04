#!/bin/bash

# データの初期化

# Borrowerの登録
echo "borrowersの初期化"
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

# Investorの登録
echo "\n"
echo "borrowersの初期化"
curl -X POST http://localhost:8080/api/investors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MUFG Bank",
    "type": "BANK",
    "investmentCapacity": 10000000,
    "currentInvestments": 0
  }'

curl -X POST http://localhost:8080/api/investors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mizuho Bank",
    "type": "BANK",
    "investmentCapacity": 20000000,
    "currentInvestments": 0
  }'

# Syndicateの登録
echo "\n"
echo "syndicatesの初期化"
curl -X POST http://localhost:8080/api/syndicates \
  -H "Content-Type: application/json" \
  -d '{
    "leadBankId": 1,
    "memberIds": [1, 2],
    "totalCommitment": 5000000
  }'

# SharePieの登録
echo "\n"
echo "share-piesの初期化"
curl -X POST http://localhost:8080/api/share-pies \
  -H "Content-Type: application/json" \
  -d '{
    "shares": {
      "1": 30,
      "2": 70
    }
  }'

# Facilityの登録
echo "\n"
echo "facilitiesの登録"
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

# FacilityInvestmentsの登録
echo "\n"
echo "facilityInvestmentsの登録"
curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "investorId": 1,
    "date": "2025-01-28T10:00:00",
    "relatedPositionId": 1
  }'

curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "investorId": 2,
    "date": "2025-01-29T10:00:00",
    "relatedPositionId": 1
  }'

# ドローダウンの登録
echo "\n"
echo "drawdownsの登録"
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

# ドローダウンの実行
echo "\n"
echo "drawdownsの実行"
curl -X PUT http://localhost:8080/api/drawdowns/3/execute

# 手数料支払いの登録
echo "\n"
echo "fee-paymentsの登録"
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
echo "\n"
echo "fee-paymentsの実行"
curl -X PUT http://localhost:8080/api/fee-payments/4/execute


# 利息支払いの登録
echo "\n"
echo "interest-paymentsの登録"
curl -X POST http://localhost:8080/api/interest-payments \
  -H "Content-Type: application/json" \
  -d '{
    "loanId": 2,
    "date": "2025-06-15T10:00:00"
  }'

# 利息支払いの実行
echo "\n"
echo "interest-paymentsの実行"
curl -X PUT http://localhost:8080/api/interest-payments/5/execute
