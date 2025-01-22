# drawdown機能の実装確認と修正のやり取り (GitHub Copilot Chat版)

## 概要

Eric Evans DDD のBreakthroughの章のシンジケートローンの実装を進めている。  
FacilityInvestmentの実装が完了したので、次はDrawdownの実装を進める。  
AIエージェント(Cline)で作った土台を元に、Drawdownの実装を進める。（土台のコードは考慮漏れがあるので、それを解析して修正する）

### 環境

- 使った機能 : GitHub Copilot Chat
- LLM : Claude 3.5 Sonnetを利用

## 開始時点の実装の確認のやり取り（GitHub Copilot Chatで #cocebaseを利用）

### User
tis-abe-akira  
#codebase DrawdownController.javaの@PostMappingのエンドポイント（メソッド名:create）を実行した時、最終的にどのような流れで処理が実行されますか？コラボレートするクラス名#メソッド名を用いて具体的に説明してください！

### Used references

Used 27 references
- README.md
- DrawdownDto.java
- Drawdown.java
- DrawdownRepository.java
- AbstractBaseService.java
- BaseService.java
- DrawdownService.java
- FeePaymentService.java
- InterestPaymentService.java
- PrincipalPaymentService.java
- BorrowerController.java
- InvestorController.java
- SyndicateController.java
- FacilityController.java
- SharePieController.java
- DrawdownController.java
- FacilityInvestmentController.java
- FacilityTradeController.java
- FeePaymentController.java
- InterestPaymentController.java
- PrincipalPaymentController.java
- spec.md
- technical-backend.md

### Answer

// 処理フロー
1. [`DrawdownController#create`](backend/src/main/java/com/syndicated_loan/syndicated_loan/feature/transaction/DrawdownController.java) 
   - HTTPリクエストボディからDrawdownDtoを受け取る
   - `drawdownService.create()`を呼び出し

2. [`DrawdownService#create`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - AbstractBaseServiceから継承した`create`メソッドを実行
   - `toEntity()`メソッドを呼び出しDTOからエンティティに変換

3. [`DrawdownService#toEntity`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - DrawdownDtoからDrawdownエンティティを生成
   - [`FacilityService#findById`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/FacilityService.java)を呼び出し関連ファシリティを取得
   - 基本プロパティを設定

4. [`DrawdownRepository#save`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/repository/DrawdownRepository.java)
   - JpaRepositoryのsaveメソッドでDrawdownエンティティを保存

5. [`DrawdownService#toDto`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - 保存されたエンティティをDTOに変換
   - 残額や利用率などの付加情報を計算して設定

最後に、作成されたDrawdownDtoをJSON形式でクライアントに返却します。

主な検証・計算処理:
- ファシリティの存在チェック
- 金額の正当性チェック
- ファシリティ残額の計算
- 利用率の計算// 処理フロー
1. [`DrawdownController#create`](backend/src/main/java/com/syndicated_loan/syndicated_loan/feature/transaction/DrawdownController.java) 
   - HTTPリクエストボディからDrawdownDtoを受け取る
   - `drawdownService.create()`を呼び出し

2. [`DrawdownService#create`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - AbstractBaseServiceから継承した`create`メソッドを実行
   - `toEntity()`メソッドを呼び出しDTOからエンティティに変換

3. [`DrawdownService#toEntity`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - DrawdownDtoからDrawdownエンティティを生成
   - [`FacilityService#findById`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/FacilityService.java)を呼び出し関連ファシリティを取得
   - 基本プロパティを設定

4. [`DrawdownRepository#save`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/repository/DrawdownRepository.java)
   - JpaRepositoryのsaveメソッドでDrawdownエンティティを保存

5. [`DrawdownService#toDto`](backend/src/main/java/com/syndicated_loan/syndicated_loan/common/service/DrawdownService.java)
   - 保存されたエンティティをDTOに変換
   - 残額や利用率などの付加情報を計算して設定

最後に、作成されたDrawdownDtoをJSON形式でクライアントに返却します。

主な検証・計算処理:
- ファシリティの存在チェック
- 金額の正当性チェック
- ファシリティ残額の計算
- 利用率の計算

### 所感（チャットを分析してakriaabeが記載）

処理の流れの説明という観点では、大筋合っている。

ただし、この文脈では、DrawdownService#toEntityから基底クラスTransactionService#setBasePropertiesを呼び出しているという情報が重要だが、これが抜けている。


## 修正のやり取り（GitHub Copilot Chat）

処理の流れがわかったので修正を進める。

### User
調査の結果、現状は仕様の考慮漏れがあることがわかりました。  
TransactionService#setBasePropertiesでは、 AmoutPieを設定する際に、すでに関連するAmountPieがあるものとして、処理（Entity永続化に関連を保持）しています。  
しかし、Drawdownの時には、入力のパラメータとしてAmountPieの生成情報を受け取り処理するように変更が必要です。 
ソースコードの修正を提案してください。

### Used references

- FeePaymentService.java
- InterestPaymentService.java
- TransactionService.java
- technical-backend.md

### Answer

以下のステップでTransactionServiceの修正を行います：

1. TransactionServiceに新しいメソッドを追加し、AmountPie生成情報を扱えるようにする
2. DrawdownServiceでAmountPieの生成処理を実装

Step 1: TransactionServiceの修正

```java
@Slf4j
public abstract class TransactionService<E extends Transaction, D extends TransactionDto, R extends JpaRepository<E, Long>> {
    // ...existing code...

    /**
     * AmountPie生成情報を含むベースプロパティの設定
     */
    protected void setBasePropertiesWithNewAmountPie(E entity, D dto, AmountPieDto amountPieDto) {
        // Positionの設定
        Position position = positionService.findById(dto.getRelatedPositionId())
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        entity.setRelatedPosition(position);

        // AmountPieの生成と設定
        if (amountPieDto != null) {
            AmountPie amountPie = amountPieService.createNewAmountPie(amountPieDto);
            entity.setAmountPie(amountPie);
        }

        // ステータスの初期設定
        entity.setStatus("PENDING");
    }

    // ...existing code...
}
```

Step 2: DrawdownServiceの修正

```java
@Service
@Transactional(readOnly = true)
public class DrawdownService extends TransactionService<Drawdown, DrawdownDto, DrawdownRepository> {
    
    @Override
    public Drawdown toEntity(DrawdownDto dto) {
        Drawdown entity = new Drawdown();
        entity.setId(dto.getId());
        entity.setType("DRAWDOWN");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        // 関連するファシリティの設定
        Facility facility = facilityService.findById(dto.getFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setFacility(facility);

        // AmountPie生成情報を含むベースプロパティの設定
        setBasePropertiesWithNewAmountPie(entity, dto, dto.getNewAmountPie());

        entity.setDrawdownAmount(dto.getDrawdownAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    // ...existing code...
}
```

Step 3: DrawdownDtoの修正

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawdownDto extends TransactionDto {
    // ...existing fields...
    
    private AmountPieDto newAmountPie; // AmountPie生成情報を追加
    
    // ...existing code...
}
```

この修正により：

1. Drawdownのリクエスト時にAmountPie生成情報を含められるようになります
2. TransactionServiceで新規AmountPie生成のケースに対応できます
3. 既存の他のTransaction処理（InterestPayment, FeePayment）への影響を最小限に抑えられます

### 所感（チャットを分析してakriaabeが記載）

ほとんど合っているが、以下のように微妙に間違ったコードを提案してくる。
かなり惜しい！！


Step1の提案

```java
        // AmountPieの生成と設定
        if (amountPieDto != null) {
-            AmountPie amountPie = amountPieService.createNewAmountPie(amountPieDto);
+            AmountPie amountPie = amountPieService.toEntity(amountPieDto);
            entity.setAmountPie(amountPie);
        }

```



Step2の提案

```java

        // 関連するファシリティの設定
-        Facility facility = facilityService.findById(dto.getFacilityId())
+        Facility facility = facilityService.findById(dto.getRelatedFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setFacility(facility);

```

## その他の修正

curlコマンドとDTOの不整合があったので、今回はcurlコマンドを修正した

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

amountのセット漏れがあったので、以下のように修正した。
（そもそも、基底クラスと拡張クラスの両方に金額を持っているので、基底クラスに寄せる修正をしたい。これは、別途対応する。）

```java

    @Override
    public Drawdown toEntity(DrawdownDto dto) {
        Drawdown entity = new Drawdown();
        entity.setId(dto.getId());
        entity.setType("DRAWDOWN");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getDrawdownAmount()); // 修正
        entity.setDrawdownAmount(dto.getDrawdownAmount()); // 修正
```


