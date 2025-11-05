create table product (
    id bigserial primary key,
    name varchar(255) not null,
    shop_id varchar(255) not null,
    shop_name varchar(255) not null,
    description varchar(255),
    images varchar(255) array,
    categories varchar(255) array default '{general}',
    stock integer default 1,
    price numeric(9,2) default 0.01,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    active boolean default true
);