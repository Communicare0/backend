-- =========================================
-- V3__insert_sample_restaurants.sql  (PostgreSQL)
-- =========================================
-- RestaurantType enum 업데이트 및 샘플 식당 데이터 추가
-- - restaurant_type에 KOR, JAP, CHI, VET 추가

SET search_path TO communicare, public;

-- ========== UPDATE RESTAURANT_TYPE ENUM ==========
-- 기존 restaurant_type 타입을 새로운 타입으로 교체
-- 1. 새로운 restaurant_type 타입 생성
CREATE TYPE communicare.restaurant_type_new AS ENUM ('HALAL','KOSHER','VEGAN', 'KOREA', 'JAPAN', 'CHINA', 'VIETNAM', 'INDIA', 'WEST', 'NONE');

-- 2. 기존 데이터 백업을 위한 임시 컬럼 추가
ALTER TABLE communicare.restaurants ADD COLUMN IF NOT EXISTS restaurant_type_backup communicare.restaurant_type;

-- 3. 기존 데이터 백업
UPDATE communicare.restaurants SET restaurant_type_backup = restaurant_type WHERE restaurant_type IS NOT NULL;

-- 4. 기존 restaurant_type 컬럼 삭제
ALTER TABLE communicare.restaurants DROP COLUMN restaurant_type;

-- 5. 새로운 restaurant_type 컬럼 추가
ALTER TABLE communicare.restaurants ADD COLUMN restaurant_type communicare.restaurant_type_new DEFAULT 'NONE';

-- 6. 데이터 복원 및 설정
UPDATE communicare.restaurants
SET restaurant_type = restaurant_type_backup::text::communicare.restaurant_type_new
WHERE restaurant_type_backup IS NOT NULL;

-- 7. 임시 백업 컬럼 삭제
ALTER TABLE communicare.restaurants DROP COLUMN restaurant_type_backup;

-- 8. 기존 타입 삭제 및 새로운 타입으로 이름 변경
DROP TYPE communicare.restaurant_type;
ALTER TYPE communicare.restaurant_type_new RENAME TO restaurant_type;

-- ========== INSERT SAMPLE RESTAURANTS ==========
INSERT INTO communicare.restaurants (
    restaurant_id,
    name,
    status,
    restaurant_type,
    rating_count,
    rating_sum,
    avg_rating,
    google_map_url,
    created_at,
    updated_at
) VALUES
(
    gen_random_uuid(),
    '휘얼 아주대점',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=11460551454271712379',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '규동집 아주대점',
    'VISIBLE',
    'JAPAN',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=4457349816920296948',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '뚱보아저씨 수제돈까스 아주대점',
    'VISIBLE',
    'JAPAN',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=7797212075679167037',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '면식당 아주대점',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=147414990123340162',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '키와마루아지',
    'VISIBLE',
    'JAPAN',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=13205503329215091106',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '한잔한식',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=12544035169867242525',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '사랑집',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=15757105276291634979',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '한마리정육식당',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=10783896134216440273',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '짜장맛좀볼래요',
    'VISIBLE',
    'CHINA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=10197913264453944278',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '보배반점 아주대점',
    'VISIBLE',
    'CHINA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=2484056679450189030',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '홍콩반점 아주대점',
    'VISIBLE',
    'CHINA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=11039054903896301541',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '우만주옥',
    'VISIBLE',
    'KOREA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=11660610810546819278',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '겐코 아주대점',
    'VISIBLE',
    'JAPAN',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=12879580095095622440',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '파스타 앤 그릴',
    'VISIBLE',
    'WEST',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=18394393329593265302',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '이태리밥',
    'VISIBLE',
    'WEST',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=14915689092460985156',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '폼탄',
    'VISIBLE',
    'WEST',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=7810576756822367277',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '고칸 아주대점',
    'VISIBLE',
    'JAPAN',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=7584694897680033826',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '미스사이공 아주대점',
    'VISIBLE',
    'VIETNAM',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=6997888067495961039',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '아주쌀국수',
    'VISIBLE',
    'VIETNAM',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=964679961367822956',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '포메인아주대점',
    'VISIBLE',
    'VIETNAM',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=6394481810193111612',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '호앙비엣 광교점',
    'VISIBLE',
    'VIETNAM',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=10877667145355865262',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '별미떡볶이',
    'VISIBLE',
    'NONE',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=3665738105376472828',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '떡장 아주대직영점',
    'VISIBLE',
    'NONE',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=14632851858511166411',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '동떡이 아주대점',
    'VISIBLE',
    'NONE',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=17767900686700782102',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '아롤도그',
    'VISIBLE',
    'NONE',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=15151611386663269277',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '올레분식우만점',
    'VISIBLE',
    'NONE',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=7017960748988653043',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '난 아주대점',
    'VISIBLE',
    'INDIA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=302140656050241995',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '샹그리라',
    'VISIBLE',
    'INDIA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=9941196853037459576',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '시타라 광교점',
    'VISIBLE',
    'INDIA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=14730014172642028781',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '갠지스 광교점',
    'VISIBLE',
    'INDIA',
    0,
    0,
    0.00,
    'https://maps.google.com/?cid=14573926349472841281',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    'IXLOS INTERNATIONAL',
    'VISIBLE',
    'HALAL',
    0,
    0,
    0.00,
    'https://maps.app.goo.gl/CKKNsFkgrXAuVwsu7',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '히말라야 정원',
    'VISIBLE',
    'HALAL',
    0,
    0,
    0.00,
    'https://maps.app.goo.gl/nEG3hCjgK1wd1QnbA',
    NOW(),
    NOW()
),
(
    gen_random_uuid(),
    '웰컴투두바이 Welcome to Dubai',
    'VISIBLE',
    'HALAL',
    0,
    0,
    0.00,
    'https://maps.app.goo.gl/psczkMD8bheXPskA9',
    NOW(),
    NOW()
);
