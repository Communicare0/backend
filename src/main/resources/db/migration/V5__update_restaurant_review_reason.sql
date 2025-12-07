-- =========================================
-- V5__update_restaurant_review_reason.sql  (PostgreSQL)
-- =========================================
-- RestaurantReview reason field 업데이트
-- - rating_good_reason, rating_bad_reason, rating_other_reason을
-- - 단일 'reason' TEXT 필드로 통합

SET search_path TO communicare, public;

-- ========== UPDATE RESTAURANT_REVIEWS TABLE ==========
-- 1. 새로운 reason 컬럼 추가
ALTER TABLE communicare.restaurant_reviews
ADD COLUMN IF NOT EXISTS reason TEXT;

-- 2. 기존 데이터를 새로운 reason 필드로 마이그레이션
-- 순서대로 good, bad, other reason을 확인하고 첫 번째로 있는 값을 reason으로 설정
UPDATE communicare.restaurant_reviews
SET reason = CASE
    WHEN rating_good_reason IS NOT NULL THEN rating_good_reason::TEXT
    WHEN rating_bad_reason IS NOT NULL THEN rating_bad_reason::TEXT
    WHEN rating_other_reason IS NOT NULL THEN rating_other_reason
    ELSE NULL
END
WHERE reason IS NULL;

-- 3. 기존 rating reason 관련 컬럼 삭제
ALTER TABLE communicare.restaurant_reviews
DROP COLUMN IF EXISTS rating_good_reason,
DROP COLUMN IF EXISTS rating_bad_reason,
DROP COLUMN IF EXISTS rating_other_reason;

-- 4. 불필요한 ENUM 타입 삭제
DROP TYPE IF EXISTS communicare.rating_good_reason;
DROP TYPE IF EXISTS communicare.rating_bad_reason;

-- =========================================
-- 마이그레이션 완료
-- =========================================