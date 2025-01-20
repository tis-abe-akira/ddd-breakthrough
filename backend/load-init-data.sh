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

echo "syndicatesの初期化"
curl -X POST http://localhost:8080/api/syndicates \
  -H "Content-Type: application/json" \
  -d '{
    "leadBankId": 1,
    "memberIds": [1, 2],
    "totalCommitment": 5000000
  }'

echo "share-piesの初期化"
curl -X POST http://localhost:8080/api/share-pies \
  -H "Content-Type: application/json" \
  -d '{
    "shares": {
      "1": 30,
      "2": 70
    }
  }'


