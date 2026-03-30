INSERT INTO "currency" ("code", "name") VALUES
('PLN', 'Polish Zloty'),
('USD', 'US Dollar'),
('EUR', 'Euro'),
('GBP', 'British Pound')
ON CONFLICT (code) DO NOTHING;
