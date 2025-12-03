-- =========================================
-- V2__update_restaurant_table.sql  (PostgreSQL)
-- =========================================
-- Restaurant 엔티티 변경사항 반영
-- - googleMapUrl 컬럼 추가
-- - name_localized, photo_url, address, phone, website 컬럼 제거
-- - Nationality enum 타입 추가

SET search_path TO communicare, public;

-- ========== NEW ENUM TYPES ==========
CREATE TYPE communicare.nationality AS ENUM ('KOREAN','VIETNAMESE','CHINESE','MYANMARESE','JAPANESE','INDONESIAN','MALAYSIAN','EMIRATIS');
CREATE TYPE communicare.preferred_food_type AS ENUM ('HALAL','KOSHER','VEGAN','NONE');

-- googleMapUrl 컬럼 추가
ALTER TABLE communicare.restaurants
ADD COLUMN google_map_url VARCHAR(255);

-- restaurant_type 컬럼의 기본값을 NONE으로 설정
ALTER TABLE communicare.restaurants
ALTER COLUMN restaurant_type SET DEFAULT 'NONE';

-- 기존 NULL 값들을 기본값으로 업데이트
UPDATE communicare.restaurants
SET restaurant_type = 'NONE'
WHERE restaurant_type IS NULL;

-- restaurant_type 컬럼을 NOT NULL로 변경
ALTER TABLE communicare.restaurants
ALTER COLUMN restaurant_type SET NOT NULL;

-- ========== USERS TABLE UPDATES ==========

-- nationality 컬럼을 VARCHAR에서 ENUM 타입으로 변경
-- 1. 기존 nationality 컬럼 데이터 백업 (임시 컬럼)
ALTER TABLE communicare.users ADD COLUMN IF NOT EXISTS nationality_backup VARCHAR(50);
UPDATE communicare.users SET nationality_backup = nationality WHERE nationality IS NOT NULL;

-- 2. 기존 nationality 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN IF EXISTS nationality;

-- 3. 새로운 ENUM 타입의 nationality 컬럼 추가
ALTER TABLE communicare.users ADD COLUMN nationality communicare.nationality;

-- 4. 데이터 복원 (기존 VARCHAR 값을 ENUM으로 변환)
UPDATE communicare.users SET nationality = 'KOREAN' WHERE nationality_backup ILIKE '%korean%';
UPDATE communicare.users SET nationality = 'VIETNAMESE' WHERE nationality_backup ILIKE '%vietnamese%';
UPDATE communicare.users SET nationality = 'CHINESE' WHERE nationality_backup ILIKE '%chinese%';
UPDATE communicare.users SET nationality = 'MYANMARESE' WHERE nationality_backup ILIKE '%myanmar%';
UPDATE communicare.users SET nationality = 'JAPANESE' WHERE nationality_backup ILIKE '%japanese%';
UPDATE communicare.users SET nationality = 'INDONESIAN' WHERE nationality_backup ILIKE '%indonesian%';
UPDATE communicare.users SET nationality = 'MALAYSIAN' WHERE nationality_backup ILIKE '%malaysian%';
UPDATE communicare.users SET nationality = 'EMIRATIS' WHERE nationality_backup ILIKE '%emirati%';

-- 5. 백업 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN IF EXISTS nationality_backup;

-- preferred_food_type 컬럼 추가 (기본값 NONE)
ALTER TABLE communicare.users ADD COLUMN preferred_food_type communicare.preferred_food_type DEFAULT 'NONE';

-- 기존 NULL 값들을 기본값으로 업데이트
UPDATE communicare.users SET preferred_food_type = 'NONE' WHERE preferred_food_type IS NULL;

-- language 컬럼을 VARCHAR에서 ENUM 타입으로 변경
-- 1. 기존 language 컬럼 데이터 백업 (임시 컬럼)
ALTER TABLE communicare.users ADD COLUMN IF NOT EXISTS language_backup VARCHAR(10);
UPDATE communicare.users SET language_backup = language WHERE language IS NOT NULL;

-- 2. 기존 language 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN IF EXISTS language;

-- 3. 새로운 ENUM 타입의 language 컬럼 추가
ALTER TABLE communicare.users ADD COLUMN language communicare.language DEFAULT 'KO';

-- 4. 데이터 복원 (기존 VARCHAR 값을 ENUM으로 변환)
UPDATE communicare.users SET language = 'KO' WHERE language_backup ILIKE '%ko%';
UPDATE communicare.users SET language = 'EN' WHERE language_backup ILIKE '%en%';
UPDATE communicare.users SET language = 'ZH' WHERE language_backup ILIKE '%zh%';
UPDATE communicare.users SET language = 'JA' WHERE language_backup ILIKE '%ja%';
UPDATE communicare.users SET language = 'ES' WHERE language_backup ILIKE '%es%';
UPDATE communicare.users SET language = 'FR' WHERE language_backup ILIKE '%fr%';
UPDATE communicare.users SET language = 'DE' WHERE language_backup ILIKE '%de%';
UPDATE communicare.users SET language = 'RU' WHERE language_backup ILIKE '%ru%';
UPDATE communicare.users SET language = 'AR' WHERE language_backup ILIKE '%ar%';
UPDATE communicare.users SET language = 'OTHER' WHERE language_backup IS NULL OR language_backup = '';

-- 5. 백업 컬럼 삭제
ALTER TABLE communicare.users DROP COLUMN IF EXISTS language_backup;

-- ========== RESTAURANTS TABLE UPDATES ==========

-- 기존 컬럼들 제거 (데이터가 필요없다면 바로 삭제)
ALTER TABLE communicare.restaurants
DROP COLUMN IF EXISTS name_localized,
DROP COLUMN IF EXISTS photo_url,
DROP COLUMN IF EXISTS address,
DROP COLUMN IF EXISTS phone,
DROP COLUMN IF EXISTS website;