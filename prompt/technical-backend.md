# バックエンドの技術要素

SpringBoot3.2
Java17
Maven3
Windows上で実行します
初期段階では、H2を使う
Windows10

JavaとMavenはインストール済みなので、セットアップは不要です。
また、Mavenプロジェクトもbackendディレクトリに初期セットアップ済みです。

## アーキテクチャー（レイヤー構造）

以下のレイヤー構造に準じてください

- RestController(DTOに依存する。Entityは直接使わない）
- Service(Entiyに依存してよい。したがって、DTOとEntityの変換はこのレイヤーでやる）
- Repository(SpringDataJPAを用いる）
- Entity

その他共通

- 例外処理はRestCotrollerでやらずに、Advisorに委譲する
- AOPを用いて、RestController、Serviceのpublicメソッドの開始、終了時にInfoログを出力する

## パッケージ構造

大きくは以下の構造にしてください。

RepositoyとEntityは全ユースケースで共通
RestController、Sevidceはfeature(usecase)単位のパッケージの下に置く（今回は.syndicated-loan以下に配置するという意味）
