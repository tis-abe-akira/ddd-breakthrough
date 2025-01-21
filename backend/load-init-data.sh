#!/bin/bash

# データの初期化

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

echo "\n"
echo "syndicatesの初期化"
curl -X POST http://localhost:8080/api/syndicates \
  -H "Content-Type: application/json" \
  -d '{
    "leadBankId": 1,
    "memberIds": [1, 2],
    "totalCommitment": 5000000
  }'

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

echo "\n"
echo "facilitiesの初期化"
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

echo "\n"
echo "facilityInvestmentsの初期化"
curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": 1,
    "investorId": 1,
    "date": "2024-01-28T10:00:00",
    "processedDate": "2024-01-29T10:00:00",
    "relatedPositionId": 1
  }'

curl -X POST http://localhost:8080/api/facility-investments \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": 1,
    "investorId": 2,
    "date": "2024-01-29T10:00:00",
    "processedDate": "2024-01-30T10:00:00",
    "relatedPositionId": 1
  }'


