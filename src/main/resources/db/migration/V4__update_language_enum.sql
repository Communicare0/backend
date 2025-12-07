-- =========================================
-- V4__update_language_enum.sql  (PostgreSQL)
-- =========================================
-- Language enum 업데이트
-- - 기존 약식 코드(KO, EN, ZH 등)에서 전체 언어 이름으로 변경
-- - 새로운 언어 추가: VIETNAMESE, MYANMARESE, INDONESIAN, MALAYSIAN, HINDI

SET search_path TO communicare, public;

-- ========== UPDATE LANGUAGE ENUM ==========
-- 기존 language 타입을 새로운 타입으로 교체
-- 1. 새로운 language 타입 생성
CREATE TYPE communicare.language_new AS ENUM (
    'KOREAN',
    'ENGLISH',
    'VIETNAMESE',
    'CHINESE',
    'MYANMARESE',
    'JAPANESE',
    'INDONESIAN',
    'MALAYSIAN',
    'ARABIC',
    'FRENCH',
    'GERMAN',
    'SPANISH',
    'RUSSIAN',
    'HINDI',
    'OTHER'
);

-- 2. 기존 데이터 백업을 위한 임시 컬럼 추가 (users 테이블)
ALTER TABLE communicare.users ADD COLUMN IF NOT EXISTS language_backup communicare.language;

-- 3. 기존 데이터 백업
UPDATE communicare.users SET language_backup = language WHERE language IS NOT NULL;

-- 4. 기존 language 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN language;

-- 5. 새로운 language 컬럼 추가
ALTER TABLE communicare.users ADD COLUMN language communicare.language_new DEFAULT 'KOREAN';

-- 6. 데이터 변환 및 복원 (약식 코드 -> 전체 이름)
UPDATE communicare.users SET language = 'KOREAN'     WHERE language_backup = 'KO';
UPDATE communicare.users SET language = 'ENGLISH'    WHERE language_backup = 'EN';
UPDATE communicare.users SET language = 'CHINESE'    WHERE language_backup = 'ZH';
UPDATE communicare.users SET language = 'JAPANESE'   WHERE language_backup = 'JA';
UPDATE communicare.users SET language = 'SPANISH'    WHERE language_backup = 'ES';
UPDATE communicare.users SET language = 'FRENCH'     WHERE language_backup = 'FR';
UPDATE communicare.users SET language = 'GERMAN'     WHERE language_backup = 'DE';
UPDATE communicare.users SET language = 'RUSSIAN'    WHERE language_backup = 'RU';
UPDATE communicare.users SET language = 'ARABIC'     WHERE language_backup = 'AR';
UPDATE communicare.users SET language = 'OTHER'      WHERE language_backup = 'OTHER';

-- 7. 임시 백업 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN IF EXISTS language_backup;

-- 8. post_translated 테이블의 language 컬럼도 동일하게 업데이트
ALTER TABLE communicare.post_translated ADD COLUMN IF NOT EXISTS language_backup communicare.language;

UPDATE communicare.post_translated SET language_backup = language WHERE language IS NOT NULL;

ALTER TABLE communicare.post_translated DROP COLUMN language;

ALTER TABLE communicare.post_translated ADD COLUMN language communicare.language_new DEFAULT 'KOREAN';

UPDATE communicare.post_translated SET language = 'KOREAN'     WHERE language_backup = 'KO';
UPDATE communicare.post_translated SET language = 'ENGLISH'    WHERE language_backup = 'EN';
UPDATE communicare.post_translated SET language = 'CHINESE'    WHERE language_backup = 'ZH';
UPDATE communicare.post_translated SET language = 'JAPANESE'   WHERE language_backup = 'JA';
UPDATE communicare.post_translated SET language = 'SPANISH'    WHERE language_backup = 'ES';
UPDATE communicare.post_translated SET language = 'FRENCH'     WHERE language_backup = 'FR';
UPDATE communicare.post_translated SET language = 'GERMAN'     WHERE language_backup = 'DE';
UPDATE communicare.post_translated SET language = 'RUSSIAN'    WHERE language_backup = 'RU';
UPDATE communicare.post_translated SET language = 'ARABIC'     WHERE language_backup = 'AR';
UPDATE communicare.post_translated SET language = 'OTHER'      WHERE language_backup = 'OTHER';

ALTER TABLE communicare.post_translated DROP COLUMN IF EXISTS language_backup;

-- 9. 기존 타입 삭제 및 새로운 타입으로 이름 변경
DROP TYPE communicare.language;
ALTER TYPE communicare.language_new RENAME TO language;

-- =========================================
-- 마이그레이션 완료
-- =========================================