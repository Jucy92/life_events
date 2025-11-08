-- 컬럼명 변경: giver_name → name, giver_relation → relation
-- 이유: 양방향 거래(받음/보냄) 모두를 명확하게 표현하기 위함

ALTER TABLE gift_money RENAME COLUMN giver_name TO name;
ALTER TABLE gift_money RENAME COLUMN giver_relation TO relation;
