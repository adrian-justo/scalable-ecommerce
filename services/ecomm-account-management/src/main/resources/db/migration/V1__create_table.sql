create table users (
    id uuid primary key,
    username varchar(255) not null unique,
    email varchar(255) unique,
    mobile_no varchar(255) unique,
    password varchar(255),
    name varchar(255),
    shop_name varchar(255),
    address varchar(255),
    roles varchar(255) array default '{BUYER}',
    notification_types varchar(255) array,
    account_id varchar(255),
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    active boolean default true
);