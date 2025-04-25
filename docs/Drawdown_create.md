# DrawdownController#createの処理フロー図

```mermaid
sequenceDiagram
    autonumber
    actor Client as クライアント
    participant DC as DrawdownController
    participant DS as DrawdownService
    participant TS as TransactionService
    participant APS as AmountPieService
    participant DR as DrawdownRepository
    participant LS as LoanService
    participant FC as FacilityService

    Client->>DC: POST /api/drawdowns (DrawdownDto)
    DC->>DS: create(dto)
    
    alt dto.getAmountPie() != null
        DS->>APS: create(dto.getAmountPie())
        APS-->>DS: 作成されたAmountPieDto
        DS->>DS: dto.setAmountPieId(amountPie.getId())
    end
    
    DS->>TS: super.create(dto)
    TS->>DS: toEntity(dto) ※抽象メソッド
    
    DS->>FC: findById(dto.getRelatedFacilityId())
    FC-->>DS: Optional<FacilityDto>
    DS->>FC: toEntity(facilityDto)
    FC-->>DS: Facility
    
    DS->>LS: create(loanDto)
    LS-->>DS: 作成されたLoanDto
    DS->>LS: toEntity(savedLoan)
    LS-->>DS: Loan
    
    alt dto.getAmountPieId() != null
        DS->>APS: findById(dto.getAmountPieId())
        APS-->>DS: Optional<AmountPieDto>
        DS->>APS: toEntity(amountPieDto)
        APS-->>DS: AmountPie
    end
    
    DS-->>TS: 変換されたDrawdownエンティティ
    TS->>DR: save(entity)
    DR-->>TS: 保存されたDrawdownエンティティ
    TS->>DS: toDto(savedEntity) ※抽象メソッド
    TS-->>DS: DrawdownDto
    
    DS->>DR: findById(createdDto.getId())
    DR-->>DS: Optional<Drawdown>
    
    DS->>LS: generateRepaymentSchedules((Loan)drawdown.getRelatedPosition())
    LS-->>DS: void
    
    DS-->>DC: 作成されたDrawdownDto
    DC-->>Client: ResponseEntity<DrawdownDto>
```

<タスク分析>

- 主要タスク: 「マジ解説！」と書かれているギャル口調の解説文をビジネスモードの口調に変換
- 重要要件:
  - ビジネスモードの口調に変更する
  - 既存の解説内容は保持する
  - この場限りの変更であること
- 潜在的課題:
  - 専門用語を適切なビジネス表現に置き換える必要がある
  - ギャル語特有の表現を適切なビジネス表現に置き換える
  - 内容の正確性を損なわないようにする

実行ステップ:

1. 既存のマーメイド図のシーケンス図部分はそのまま残す
2. 「マジ解説！」の見出しをビジネスライクな見出しに変更
3. 各文章をギャル語からビジネスモードに変換
4. 技術的な内容は維持しつつ、表現方法だけ変更
5. 全体の流れを確認し、ビジネスモードとして適切な表現になっているか確認
</タスク分析>

# DrawdownController#createの処理フロー図

```mermaid
sequenceDiagram
    autonumber
    actor Client as クライアント
    participant DC as DrawdownController
    participant DS as DrawdownService
    participant TS as TransactionService
    participant APS as AmountPieService
    participant DR as DrawdownRepository
    participant LS as LoanService
    participant FC as FacilityService

    Client->>DC: POST /api/drawdowns (DrawdownDto)
    DC->>DS: create(dto)
    
    alt dto.getAmountPie() != null
        DS->>APS: create(dto.getAmountPie())
        APS-->>DS: 作成されたAmountPieDto
        DS->>DS: dto.setAmountPieId(amountPie.getId())
    end
    
    DS->>TS: super.create(dto)
    TS->>DS: toEntity(dto) ※抽象メソッド
    
    DS->>FC: findById(dto.getRelatedFacilityId())
    FC-->>DS: Optional<FacilityDto>
    DS->>FC: toEntity(facilityDto)
    FC-->>DS: Facility
    
    DS->>LS: create(loanDto)
    LS-->>DS: 作成されたLoanDto
    DS->>LS: toEntity(savedLoan)
    LS-->>DS: Loan
    
    alt dto.getAmountPieId() != null
        DS->>APS: findById(dto.getAmountPieId())
        APS-->>DS: Optional<AmountPieDto>
        DS->>APS: toEntity(amountPieDto)
        APS-->>DS: AmountPie
    end
    
    DS-->>TS: 変換されたDrawdownエンティティ
    TS->>DR: save(entity)
    DR-->>TS: 保存されたDrawdownエンティティ
    TS->>DS: toDto(savedEntity) ※抽象メソッド
    TS-->>DS: DrawdownDto
    
    DS->>DR: findById(createdDto.getId())
    DR-->>DS: Optional<Drawdown>
    
    DS->>LS: generateRepaymentSchedules((Loan)drawdown.getRelatedPosition())
    LS-->>DS: void
    
    DS-->>DC: 作成されたDrawdownDto
    DC-->>Client: ResponseEntity<DrawdownDto>
```

## 処理フローの詳細解説

1. クライアントがHTTP POSTリクエストによりドローダウンの情報をコントローラに送信します。
2. コントローラはDrawdownServiceのcreateメソッドに処理を委譲します。
3. DrawdownServiceではAmountPie（資金の分配情報）が存在する場合、先にAmountPieServiceを利用して作成処理を行います。
4. 続いて親クラスであるTransactionServiceのcreateメソッドを呼び出し、基本的な登録処理を実行します。
5. DTOからエンティティへの変換処理はDrawdownServiceで実装されており、
6. その過程で関連するFacilityの取得、
7. 新規Loanエンティティの作成と永続化、
8. AmountPieの設定など複数の処理が行われる構成となっています。
9. エンティティの永続化後、返済スケジュール生成のためにLoanServiceが呼び出されます。
10. 最終的に作成されたドローダウンのDTOがクライアントに返却されます。

このような処理フローは、ドメイン駆動設計の観点から見ると適切に責務が分散されており、トランザクションの一貫性を保ちつつ、各コンポーネントが連携する優れた設計となっています。
