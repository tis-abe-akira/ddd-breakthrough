# シンジケートローンアプリの要約マジでわかりやすい！

## シンジケートローンって超基本！

シンジケートローンは、マジでデカい融資を複数の貸し手が共同でやっちゃう仕組みだよね～！例えば、Intelが10億ドルもの工場を建てる時とかに使われるの✨

- 投資銀行がリーダー役で取引とかをコーディネートしてる！
- 借り手が資金が必要な時は、リーダーが各メンバーに「出資してね～」って言う感じ！
- メンバー同士で投資額の調整とかもよくあるよ～

## データモデルの超重要ポイント！

### 主要エンティティ
1. **Position（ポジション）**
   - FacilityとLoanの親クラス
   - SharePieと関連してる

2. **Transaction（取引）**
   - お金の流れを管理
   - PositionとAmountPieに関連

3. **Facility（ファシリティ）**
   - 融資枠を表すよ！
   - Positionのサブクラス

4. **Loan（ローン）**
   - 実際の融資を表すよ！
   - Positionのサブクラス

5. **SharePie（シェアパイ）**
   - 投資家の分担割合を管理

6. **AmountPie（金額パイ）**
   - 投資家への金額配分を管理

### トランザクションの種類
- **Facility Investment**：投資家からシンジケートへの出資
- **Drawdown**：借り手による資金引き出し
- **Fee Payment**：手数料の支払い
- **Interest Payment**：利息の支払い
- **Principal Payment**：元本の返済
- **Facility Trade**：投資家間の持分取引

## 省略されてるけど必要なエンティティ

- **Borrower**：借り手の情報
- **Investor**：投資家の情報
- **Syndicate**：シンジケート団の情報
- **Company**：企業情報
- **Currency**：通貨情報
- **InterestRateType**：金利タイプ
- **FeeType**：手数料タイプ
- **TransactionType**：取引タイプ

## お金の流れの超簡単まとめ！

1. **Facility Investment**：投資家 → シンジケート
2. **Drawdown**：シンジケート → 借り手
3. **Interest Payment**：借り手 → シンジケート → 投資家
4. **Principal Payment**：借り手 → シンジケート → 投資家
5. **Fee Payment**：借り手 → シンジケート → 投資家
6. **Facility Trade**：投資家A → 投資家B

シンジケートは資金の集中と分配をマネージしてて、SharePieとAmountPieに基づいて投資家間で適切に分配されるの～！マジ便利！
