ALTER TABLE hr_salary_batch_item
    ADD COLUMN pension_insurance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '养老保险个人缴纳金额' AFTER social_insurance,
    ADD COLUMN medical_insurance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '医疗保险个人缴纳金额' AFTER pension_insurance,
    ADD COLUMN unemployment_insurance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '失业保险个人缴纳金额' AFTER medical_insurance;

UPDATE hr_salary_batch_item
SET pension_insurance = COALESCE(social_insurance, 0.00),
    medical_insurance = COALESCE(medical_insurance, 0.00),
    unemployment_insurance = COALESCE(unemployment_insurance, 0.00)
WHERE pension_insurance = 0.00
  AND medical_insurance = 0.00
  AND unemployment_insurance = 0.00;
