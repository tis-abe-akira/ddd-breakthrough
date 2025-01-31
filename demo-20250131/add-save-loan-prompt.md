#file:Drawdown.java
#file:DrawdownDto.java
#file:DrawdownService.java
#file:TransactionService.java
#file:DrawdownController.java
#file:DrawdownRepository.java

現状は、DrawdownService#toEntityを実行しても、Loanエンティティの永続化がされません。
実装がされていないのが原因です。Loanエンティティの永続化をするように、機能追加を行ってください。

ーー以下、誘導プロンプト
Loanエンティティの永続化の際には、LoangService#toEntityを使ってください。
