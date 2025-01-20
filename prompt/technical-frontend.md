# フロントエンドの技術要素

## コア技術

- TypeScript: ^5.0.0
- Node.js: ^20.0.0  

## フロントエンド

- Next.js: ^15.1.3
- React: ^19.0.0
- Tailwind CSS: ^3.4.17
- shadcn/ui: ^2.1.8

## 開発ツール

- npm: ^10.0.0
- ESLint: ^9.0.0
- TypeScript: ^5.0.0

## 重要な制約事項

- APIクライアントは `app/lib/api/client.ts` で一元管理
- これらのファイルは変更禁止（変更が必要な場合は承認が必要）：
  - client.ts  - AIモデルとAPI設定の中核
  - types.ts   - 型定義の一元管理
  - config.ts  - 環境設定の一元管理

## 実装規則

- 型定義は必ず types.ts を参照
- 環境変数の利用は config.ts 経由のみ許可

---

## プロジェクト構成

以下のディレクトリ構造に従って実装を行ってください：

```console
my-next-app/
├── app/
│   ├── api/
│   │   └── [endpoint]/
│   │       └── route.ts
│   ├── components/
│   │   ├── ui/
│   │   └── layout/
│   ├── hooks/
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── types.ts
│   │   │   └── config.ts
│   │   └── utils/
│   └── styles/
```

### 配置ルール

- UIコンポーネント → `app/components/ui/`
- APIエンドポイント → `app/api/[endpoint]/route.ts`
- 共通処理 → `app/lib/utils/`
- API関連処理 → `app/lib/api/`
