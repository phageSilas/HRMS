ALTER TABLE hr_employee_salary_profile
    ADD COLUMN pension_insurance_base DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '养老保险基数' AFTER social_insurance_base,
    ADD COLUMN pension_insurance_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0800 COMMENT '养老保险个人缴纳比例' AFTER pension_insurance_base,
    ADD COLUMN medical_insurance_base DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '医疗保险基数' AFTER pension_insurance_rate,
    ADD COLUMN medical_insurance_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0200 COMMENT '医疗保险个人缴纳比例' AFTER medical_insurance_base,
    ADD COLUMN unemployment_insurance_base DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '失业保险基数' AFTER medical_insurance_rate,
    ADD COLUMN unemployment_insurance_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0050 COMMENT '失业保险个人缴纳比例' AFTER unemployment_insurance_base;

UPDATE hr_employee_salary_profile
SET pension_insurance_base = COALESCE(NULLIF(pension_insurance_base, 0.00), COALESCE(social_insurance_base, 0.00)),
    medical_insurance_base = COALESCE(NULLIF(medical_insurance_base, 0.00), COALESCE(social_insurance_base, 0.00)),
    unemployment_insurance_base = COALESCE(NULLIF(unemployment_insurance_base, 0.00), COALESCE(social_insurance_base, 0.00)),
    pension_insurance_rate = COALESCE(NULLIF(pension_insurance_rate, 0.0000), 0.0800),
    medical_insurance_rate = COALESCE(NULLIF(medical_insurance_rate, 0.0000), 0.0200),
    unemployment_insurance_rate = COALESCE(NULLIF(unemployment_insurance_rate, 0.0000), 0.0050);
