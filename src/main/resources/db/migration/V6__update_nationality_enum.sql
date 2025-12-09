-- =========================================
-- V6__update_nationality_enum.sql  (PostgreSQL)
-- =========================================
-- Nationality enum 타입 확장
-- - 기존 8개 국가에서 약 80개 국가로 확장
-- - 새로운 Nationality Java Enum에 맞춰 DB ENUM 타입 업데이트

SET search_path TO communicare, public;

-- ========== UPDATE NATIONALITY ENUM TYPE ==========
-- 1. 기존 ENUM 타입을 새로운 타입으로 교체하기 위해 임시 타입 생성
CREATE TYPE communicare.nationality_new AS ENUM (
    'ALGERIAN','AMERICAN','ARGENTINIAN','AUSTRALIAN','AZERBAIJANI','BANGLADESHI','BELARUSIAN',
    'BELGIAN','BRAZILIAN','BRITISH','CAMBODIAN','CANADIAN','CHILEAN','CHINESE','COLOMBIAN',
    'CUBAN','CZECH','DUTCH','ECUADORIAN','EGYPTIAN','EMIRATI','ETHIOPIAN','FILIPINO','FINNISH',
    'FRENCH','GEORGIAN','GERMAN','GHANAIAN','GREEK','HUNGARIAN','INDIAN','INDONESIAN','IRANIAN',
    'IRAQI','ITALIAN','JAMAICAN','JAPANESE','JORDANIAN','KAZAKHSTANI','KENYAN','KOREAN','KYRGYZ',
    'LAOTIAN','LEBANESE','MALAYSIAN','MEXICAN','MONGOLIAN','MOROCCAN','MYANMARESE','NEPALESE',
    'NEW_ZEALANDER','NIGERIAN','NORWEGIAN','PAKISTANI','PALESTINIAN','PERUVIAN','POLISH',
    'PORTUGUESE','QATARI','ROMANIAN','RUSSIAN','SAUDI','SINGAPOREAN','SOUTH_AFRICAN','SPANISH',
    'SRI_LANKAN','SUDANESE','SWEDISH','TAIWANESE','THAI','TURKISH','UKRAINIAN','UZBEKISTANI',
    'VENEZUELAN','VIETNAMESE','YEMENI','NONE'
);

-- 2. users 테이블의 nationality 컬럼을 임시 컬럼으로 변경
ALTER TABLE communicare.users ALTER COLUMN nationality TYPE VARCHAR(50) USING nationality::TEXT;

-- 3. 새로운 ENUM 타입으로 컬럼 변경
ALTER TABLE communicare.users ALTER COLUMN nationality TYPE communicare.nationality_new USING
    CASE
        WHEN nationality = 'KOREAN' THEN 'KOREAN'::communicare.nationality_new
        WHEN nationality = 'VIETNAMESE' THEN 'VIETNAMESE'::communicare.nationality_new
        WHEN nationality = 'CHINESE' THEN 'CHINESE'::communicare.nationality_new
        WHEN nationality = 'MYANMARESE' THEN 'MYANMARESE'::communicare.nationality_new
        WHEN nationality = 'JAPANESE' THEN 'JAPANESE'::communicare.nationality_new
        WHEN nationality = 'INDONESIAN' THEN 'INDONESIAN'::communicare.nationality_new
        WHEN nationality = 'MALAYSIAN' THEN 'MALAYSIAN'::communicare.nationality_new
        WHEN nationality = 'EMIRATIS' THEN 'EMIRATI'::communicare.nationality_new
        ELSE 'NONE'::communicare.nationality_new
    END;

-- 4. 기존 ENUM 타입 삭제
DROP TYPE IF EXISTS communicare.nationality;

-- 5. 새로운 ENUM 타입의 이름 변경
ALTER TYPE communicare.nationality_new RENAME TO nationality;

-- ========== DATA MIGRATION ==========
-- NULL 값이나 기타 미인식 데이터를 'NONE'으로 설정
UPDATE communicare.users
SET nationality = 'NONE'::communicare.nationality
WHERE nationality NOT IN (
    'ALGERIAN','AMERICAN','ARGENTINIAN','AUSTRALIAN','AZERBAIJANI','BANGLADESHI','BELARUSIAN',
    'BELGIAN','BRAZILIAN','BRITISH','CAMBODIAN','CANADIAN','CHILEAN','CHINESE','COLOMBIAN',
    'CUBAN','CZECH','DUTCH','ECUADORIAN','EGYPTIAN','EMIRATI','ETHIOPIAN','FILIPINO','FINNISH',
    'FRENCH','GEORGIAN','GERMAN','GHANAIAN','GREEK','HUNGARIAN','INDIAN','INDONESIAN','IRANIAN',
    'IRAQI','ITALIAN','JAMAICAN','JAPANESE','JORDANIAN','KAZAKHSTANI','KENYAN','KOREAN','KYRGYZ',
    'LAOTIAN','LEBANESE','MALAYSIAN','MEXICAN','MONGOLIAN','MOROCCAN','MYANMARESE','NEPALESE',
    'NEW_ZEALANDER','NIGERIAN','NORWEGIAN','PAKISTANI','PALESTINIAN','PERUVIAN','POLISH',
    'PORTUGUESE','QATARI','ROMANIAN','RUSSIAN','SAUDI','SINGAPOREAN','SOUTH_AFRICAN','SPANISH',
    'SRI_LANKAN','SUDANESE','SWEDISH','TAIWANESE','THAI','TURKISH','UKRAINIAN','UZBEKISTANI',
    'VENEZUELAN','VIETNAMESE','YEMENI','NONE'
);

-- =========================================
-- 마이그레이션 완료
-- =========================================