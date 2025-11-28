-- =========================================
-- V1__init.sql  (PostgresSQL)
-- =========================================

-- ========== SCHEMA ==========
CREATE SCHEMA IF NOT EXISTS communicare;

-- ========== ENUM TYPES (in communicare schema) ==========
CREATE TYPE communicare.account_status           AS ENUM ('ACTIVE','SUSPENDED','DELETED');
CREATE TYPE communicare.user_role                AS ENUM ('USER','ADMIN');
CREATE TYPE communicare.friendship_status        AS ENUM ('PENDING','ACCEPTED','BLOCKED');
CREATE TYPE communicare.comment_status           AS ENUM ('VISIBLE','HIDDEN','BLOCKED');
CREATE TYPE communicare.post_category            AS ENUM ('NOTICE','GENERAL','QNA');
CREATE TYPE communicare.post_status              AS ENUM ('VISIBLE','DELETED','BLOCKED');
CREATE TYPE communicare.report_target_type       AS ENUM ('POST','COMMENT');
CREATE TYPE communicare.report_status            AS ENUM ('RECEIVED','IN_PROGRESS','DONE');
CREATE TYPE communicare.chat_room_type           AS ENUM ('DIRECT','GROUP');
CREATE TYPE communicare.chat_room_status         AS ENUM ('VISIBLE','DELETED','BLOCKED');
CREATE TYPE communicare.message_type             AS ENUM ('TEXT','IMAGE','SHARED_POST');
CREATE TYPE communicare.restaurant_status        AS ENUM ('VISIBLE','DELETED','BLOCKED');
CREATE TYPE communicare.restaurant_type          AS ENUM ('HALAL','KOSHER','VEGAN','NONE');
CREATE TYPE communicare.rating_good_reason       AS ENUM ('친절해요','맛있어요','가성비좋아요','분위기좋아요');
CREATE TYPE communicare.rating_bad_reason        AS ENUM ('불친절해요','맛없어요','비싸요','위생이별로예요');
CREATE TYPE communicare.day_of_week_type         AS ENUM ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY');
CREATE TYPE communicare.language                 AS ENUM ('KO','EN','ZH','JA','ES','FR','DE','RU','AR','OTHER');

SET search_path TO communicare, public;

-- ========== 1. users ==========
CREATE TABLE communicare.users (
                     user_id           UUID PRIMARY KEY,
                     email             VARCHAR(255)  NOT NULL,
                     password          VARCHAR(255)  NOT NULL,
                     nickname          VARCHAR(50)   NOT NULL,
                     department        VARCHAR(100),
                     student_id        VARCHAR(20),
                     nationality       VARCHAR(50),
                     language          VARCHAR(10),
                     profile_image_url VARCHAR(2048),
                     friend_code       VARCHAR(10)   NOT NULL,
                     role              user_role    NOT NULL DEFAULT 'USER',
                     status            account_status NOT NULL DEFAULT 'ACTIVE',
                     created_at        TIMESTAMPTZ   NOT NULL,
                     updated_at        TIMESTAMPTZ   NOT NULL,
                     deleted_at        TIMESTAMPTZ,

                     CONSTRAINT uk_user_email       UNIQUE (email),
                     CONSTRAINT uk_user_nickname    UNIQUE (nickname),
                     CONSTRAINT uk_user_friend_code UNIQUE (friend_code)
);

-- ========== 2. friendships ==========
CREATE TABLE communicare.friendships (
                           friendship_id BIGSERIAL PRIMARY KEY,
                           requester_id  UUID      NOT NULL,
                           addressee_id  UUID      NOT NULL,
                           status        friendship_status NOT NULL DEFAULT 'PENDING',
                           created_at    TIMESTAMPTZ NOT NULL,
                           updated_at    TIMESTAMPTZ NOT NULL,
                           deleted_at    TIMESTAMPTZ,

                           CONSTRAINT fk_friend_requester FOREIGN KEY (requester_id) REFERENCES communicare.users(user_id),
                           CONSTRAINT fk_friend_addressee FOREIGN KEY (addressee_id) REFERENCES communicare.users(user_id),
                           CONSTRAINT uk_friend_pair UNIQUE (requester_id, addressee_id)
);
CREATE INDEX ix_friend_requester ON communicare.friendships (requester_id);
CREATE INDEX ix_friend_addressee ON communicare.friendships (addressee_id);

-- ========== 4. posts ==========
CREATE TABLE communicare.posts (
                     post_id      UUID PRIMARY KEY,
                     author_id    UUID       NOT NULL,
                     title        TEXT,
                     content      TEXT,
                     category     post_category,
                     is_translated BOOLEAN     NOT NULL DEFAULT FALSE,
                     status       post_status  NOT NULL DEFAULT 'VISIBLE',
                     view_count   INT          NOT NULL DEFAULT 0,
                     like_count   INT          NOT NULL DEFAULT 0,
                     created_at   TIMESTAMPTZ  NOT NULL,
                     updated_at   TIMESTAMPTZ  NOT NULL,
                     deleted_at   TIMESTAMPTZ,

                     CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES communicare.users(user_id)
);
CREATE INDEX ix_post_author ON communicare.posts (author_id);

-- ========== 2.5. post_translated ==========
CREATE TABLE communicare.post_translated (
                               post_translated_id UUID PRIMARY KEY,
                               post_id            UUID     NOT NULL,
                               language           language NOT NULL,
                               translated_title   TEXT,
                               translated_content TEXT,
                               translated_at      TIMESTAMPTZ NOT NULL,

                               CONSTRAINT uq_post_lang UNIQUE (post_id, language),
                               CONSTRAINT fk_translated_post FOREIGN KEY (post_id) REFERENCES communicare.posts(post_id)
);
CREATE INDEX ix_post_translated_post ON communicare.post_translated (post_id);

-- ========== 2.6. refresh_tokens ==========
CREATE TABLE communicare.refresh_tokens (
                                     id              BIGSERIAL PRIMARY KEY,
                                     user_id         UUID    NOT NULL,
                                     refresh_token   VARCHAR(255) NOT NULL,
                                     expires_at      TIMESTAMPTZ   NOT NULL,
                                     created_at      TIMESTAMPTZ   NOT NULL,
                                     updated_at      TIMESTAMPTZ   NOT NULL,

                                     CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES communicare.users(user_id)
);
CREATE INDEX ix_refresh_token_user ON communicare.refresh_tokens (user_id);

-- ========== 3. comments ==========
CREATE TABLE communicare.comments (
                        comment_id   UUID PRIMARY KEY,
                        post_id      UUID         NOT NULL,
                        author_id    UUID       NOT NULL,
                        parent_id    UUID,
                        content      VARCHAR(255) NOT NULL,
                        is_translated BOOLEAN     NOT NULL DEFAULT FALSE,
                        status       comment_status NOT NULL DEFAULT 'VISIBLE',
                        created_at   TIMESTAMPTZ  NOT NULL,
                        updated_at   TIMESTAMPTZ  NOT NULL,
                        deleted_at   TIMESTAMPTZ,

                        CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES communicare.posts(post_id),
                        CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES communicare.users(user_id),
                        CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES communicare.comments(comment_id)
);
CREATE INDEX ix_comment_post   ON communicare.comments (post_id);
CREATE INDEX ix_comment_author ON communicare.comments (author_id);
CREATE INDEX ix_comment_parent ON communicare.comments (parent_id);

-- ========== 5. post_likes (단일 PK + 유니크 제약) ==========
CREATE TABLE communicare.post_likes (
                          post_like_id BIGSERIAL PRIMARY KEY,
                          user_id      UUID NOT NULL,
                          post_id      UUID   NOT NULL,
                          created_at   TIMESTAMPTZ NOT NULL,
                          updated_at   TIMESTAMPTZ NOT NULL,
                          deleted_at   TIMESTAMPTZ,

                          CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES communicare.users(user_id),
                          CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES communicare.posts(post_id),
                          CONSTRAINT uk_post_like_user_post UNIQUE (user_id, post_id)
);
CREATE INDEX ix_post_like_user ON communicare.post_likes (user_id);
CREATE INDEX ix_post_like_post ON communicare.post_likes (post_id);

-- ========== 6. reports ==========
CREATE TABLE communicare.reports (
                       report_id   BIGSERIAL PRIMARY KEY,
                       reporter_id UUID NOT NULL,
                       target_type report_target_type NOT NULL,
                       target_id   UUID   NOT NULL,  -- Post.post_id 또는 Comment.comment_id
                       reason      VARCHAR(255) NOT NULL,
                       status      report_status NOT NULL DEFAULT 'RECEIVED',
                       created_at  TIMESTAMPTZ NOT NULL,
                       updated_at  TIMESTAMPTZ NOT NULL,
                       deleted_at  TIMESTAMPTZ,

                       CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES communicare.users(user_id)
);
CREATE INDEX ix_report_reporter ON communicare.reports (reporter_id);
CREATE INDEX ix_report_target   ON communicare.reports (target_type, target_id);

-- ========== 7. chat_rooms ==========
CREATE TABLE communicare.chat_rooms (
                          chat_room_id  UUID PRIMARY KEY,
                          chat_room_type chat_room_type NOT NULL,
                          title         VARCHAR(64),
                          photo_url     VARCHAR(255),
                          status        chat_room_status NOT NULL DEFAULT 'VISIBLE',
                          last_chat_id  BIGINT, -- FK는 아래 ALTER TABLE에서 추가
                          created_at    TIMESTAMPTZ NOT NULL,
                          updated_at    TIMESTAMPTZ NOT NULL,
                          deleted_at    TIMESTAMPTZ
);

-- ========== 9. chat_messages ==========
CREATE TABLE communicare.chat_messages (
                             chat_message_id BIGSERIAL PRIMARY KEY,
                             chat_room_id    UUID    NOT NULL,
                             sender_id       UUID  NOT NULL,
                             content         TEXT    NOT NULL,
                             message_type    message_type NOT NULL DEFAULT 'TEXT',
                             is_translated   BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at      TIMESTAMPTZ NOT NULL,
                             updated_at      TIMESTAMPTZ NOT NULL,
                             deleted_at      TIMESTAMPTZ,

                             CONSTRAINT fk_msg_room   FOREIGN KEY (chat_room_id) REFERENCES communicare.chat_rooms(chat_room_id),
                             CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id)    REFERENCES communicare.users(user_id)
);
CREATE INDEX ix_msg_room   ON communicare.chat_messages (chat_room_id);
CREATE INDEX ix_msg_sender ON communicare.chat_messages (sender_id);

-- chat_rooms.last_chat_id FK 추가 (순환참조 방지 위해 분리)
ALTER TABLE communicare.chat_rooms
  ADD CONSTRAINT fk_chatroom_last_chat
    FOREIGN KEY (last_chat_id) REFERENCES communicare.chat_messages(chat_message_id);

-- ========== 8. chat_room_members ==========
CREATE TABLE communicare.chat_room_members (
                                 chat_room_member_id BIGSERIAL PRIMARY KEY,
                                 chat_room_id        UUID   NOT NULL,
                                 user_id             UUID NOT NULL,
                                 last_read_message_id BIGINT,
                                 created_at          TIMESTAMPTZ NOT NULL,
                                 updated_at          TIMESTAMPTZ NOT NULL,
                                 deleted_at          TIMESTAMPTZ,

                                 CONSTRAINT fk_crm_room FOREIGN KEY (chat_room_id) REFERENCES communicare.chat_rooms(chat_room_id),
                                 CONSTRAINT fk_crm_user FOREIGN KEY (user_id)      REFERENCES communicare.users(user_id),
                                 CONSTRAINT fk_crm_last_read_msg FOREIGN KEY (last_read_message_id) REFERENCES communicare.chat_messages(chat_message_id),
                                 CONSTRAINT uk_room_user UNIQUE (chat_room_id, user_id)
);
CREATE INDEX ix_crm_room ON communicare.chat_room_members (chat_room_id);
CREATE INDEX ix_crm_user ON communicare.chat_room_members (user_id);

-- ========== 10. restaurants ==========
CREATE TABLE communicare.restaurants (
                           restaurant_id  UUID PRIMARY KEY,
                           name           VARCHAR(255) NOT NULL,
                           name_localized JSONB,
                           photo_url      VARCHAR(255),
                           status         restaurant_status NOT NULL DEFAULT 'VISIBLE',
                           restaurant_type restaurant_type,
                           rating_count   INT,
                           rating_sum     INT,
                           avg_rating     NUMERIC(3,2),
                           address        VARCHAR(256),
                           phone          VARCHAR(16),
                           website        VARCHAR(256),
                           created_at     TIMESTAMPTZ NOT NULL,
                           updated_at     TIMESTAMPTZ NOT NULL,
                           deleted_at     TIMESTAMPTZ
);

-- ========== 11. restaurant_reviews ==========
CREATE TABLE communicare.restaurant_reviews (
                                  restaurant_review_id BIGSERIAL PRIMARY KEY,
                                  restaurant_id        UUID    NOT NULL,
                                  author_id            UUID    NOT NULL,
                                  rating               INT     NOT NULL,
                                  rating_good_reason   rating_good_reason,
                                  rating_bad_reason    rating_bad_reason,
                                  rating_other_reason  TEXT,
                                  created_at           TIMESTAMPTZ NOT NULL,
                                  updated_at           TIMESTAMPTZ NOT NULL,
                                  deleted_at           TIMESTAMPTZ,

                                  CONSTRAINT fk_review_restaurant FOREIGN KEY (restaurant_id) REFERENCES communicare.restaurants(restaurant_id),
                                  CONSTRAINT fk_review_author     FOREIGN KEY (author_id)     REFERENCES communicare.users(user_id),
                                  CONSTRAINT ck_rating_range CHECK (rating BETWEEN 1 AND 5)
);
CREATE INDEX ix_review_restaurant ON communicare.restaurant_reviews (restaurant_id);
CREATE INDEX ix_review_author     ON communicare.restaurant_reviews (author_id);

-- ========== 12. notifications ==========
CREATE TABLE communicare.notifications (
                             notification_id BIGSERIAL PRIMARY KEY,
                             receiver_id     UUID NOT NULL,
                             content         VARCHAR(256) NOT NULL,
                             redirect_url    VARCHAR(2048),
                             is_read         BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at      TIMESTAMPTZ NOT NULL,
                             updated_at      TIMESTAMPTZ NOT NULL,

                             CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES communicare.users(user_id)
);
CREATE INDEX ix_notification_receiver ON communicare.notifications (receiver_id);

-- ========== 13. user_keywords ==========
CREATE TABLE communicare.user_keywords (
                             keyword_id  BIGSERIAL PRIMARY KEY,
                             user_id     UUID NOT NULL,
                             keyword     VARCHAR(50) NOT NULL,
                             created_at  TIMESTAMPTZ NOT NULL,
                             updated_at  TIMESTAMPTZ NOT NULL,

                             CONSTRAINT fk_keyword_user FOREIGN KEY (user_id) REFERENCES communicare.users(user_id),
                             CONSTRAINT uk_user_keyword UNIQUE (user_id, keyword)
);
CREATE INDEX ix_keyword_user ON communicare.user_keywords (user_id);

-- ========== 14. timetables ==========
CREATE TABLE communicare.timetables (
                          timetable_id BIGSERIAL PRIMARY KEY,
                          user_id      UUID NOT NULL,
                          day_of_week  day_of_week_type NOT NULL,
                          start_time   TIME   NOT NULL,
                          end_time     TIME   NOT NULL,
                          subject_name VARCHAR(100),
                          created_at   TIMESTAMPTZ NOT NULL,
                          updated_at   TIMESTAMPTZ NOT NULL,

                          CONSTRAINT fk_timetable_user FOREIGN KEY (user_id) REFERENCES communicare.users(user_id),
                          CONSTRAINT ck_time_order CHECK (start_time < end_time)
);
CREATE INDEX ix_timetable_user ON communicare.timetables (user_id, day_of_week);

-- =========================================
-- 끝
-- =========================================
