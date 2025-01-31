# Drawdownの作成処理フロー分析

## エントリーポイント

DrawdownController#create

- @PostMapping("/api/drawdowns")
- クライアントからDrawdownDtoを受け取る

## 処理の流れ

1. DrawdownController#create
   - DrawdownDtoをリクエストボディから受け取る
   - DrawdownService#createを呼び出し

2. DrawdownService#create (Override)
   - 独自の前処理としてAmountPieの生成を行う
   - AmountPieService#createでAmountPieを生成
   - 生成したAmountPieのIDをDTOに設定
   - 親クラスのTransactionService#createを呼び出し

3. TransactionService#create
   - DTOからエンティティに変換するためDrawdownService#toEntityを呼び出し
   - 生成したエンティティをリポジトリで保存
   - 保存したエンティティをDTOに変換して返却

4. DrawdownService#toEntity (Override)
   - 基本的なエンティティ情報を設定
   - Facilityの関連付け（FacilityService経由）
   - TransactionService#setBasePropertiesで共通項目を設定

5. TransactionService#setBaseProperties
   - Position情報の設定（PositionService経由）
   - AmountPie情報の設定（AmountPieService経由）

6. DrawdownService#toDto (Override)
   - 基本的なDTO情報を設定
   - TransactionService#setBaseDtoPropertiesで共通項目を設定
   - Facilityの情報をDTOに設定（FacilityService経由）
   - 残額と利用率の計算結果を設定

7. DrawdownController#create
   - ResponseEntityでラップして返却

## 主要なコラボレーションクラス

- DrawdownController
- DrawdownService（extends TransactionService）
- TransactionService（基底クラス）
- FacilityService
- PositionService
- AmountPieService
- DrawdownRepository

## 注意点

- Drawdownの作成時には、AmountPieの生成が必要（DrawdownService#createで対応）
- 基底クラスのTransactionServiceの処理も重要な役割を果たしている
- Facilityとの関連付けはDrawdown固有の処理
