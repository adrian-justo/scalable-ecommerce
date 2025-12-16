create table payment (
    id bigserial primary key,
    buyer_id varchar(255),
    session_id varchar(255),
    currency varchar(255),
    amount bigint,
    session_url text,
    status varchar(255),
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone
);