-- テストデータの初期化
DELETE FROM borrower;

-- サンプルデータの投入
INSERT INTO borrower (name, credit_rating, financial_statements, contact_information, company_type, industry, version)
VALUES 
('株式会社テスト', 'A', '{"revenue": "1000000000", "profit": "100000000"}', 'test@example.com', '株式会社', '製造業', 0),
('サンプル工業', 'AA', '{"revenue": "2000000000", "profit": "200000000"}', 'sample@example.com', '株式会社', '工業', 0),
('テックベンチャー', 'B', '{"revenue": "500000000", "profit": "50000000"}', 'tech@example.com', '株式会社', 'IT', 0);
