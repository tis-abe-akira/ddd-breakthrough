# DrawdownController#createの処理フロー図

```mermaid
sequenceDiagram
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
