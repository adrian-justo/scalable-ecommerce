create table orders (
    id bigserial primary key,
    buyer_id varchar(255) not null,
    shop_id varchar(255) not null,
    total numeric(11,2),
    total_products integer,
    total_quantity integer,
    tracking_number varchar(255),
    courier_code varchar(255),
    status varchar(255),
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone
);

create table delivery_information (
    order_id bigint primary key,
    name varchar(255),
    address varchar(255),
    email varchar(255),
    mobile_no varchar(255)
);

create table shop_information (
    order_id bigint primary key,
    name varchar(255),
    address varchar(255),
    email varchar(255),
    mobile_no varchar(255)
);

create table order_item (
    order_id bigint not null,
    product_id bigint not null,
    quantity integer not null,
    total_price numeric(11,2),
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    primary key (order_id, product_id)
);

create table order_item_detail (
    item_order_id bigint not null,
    item_product_id bigint not null,
    image varchar(255),
    name varchar(255),
    price numeric(9,2),
    primary key (item_order_id, item_product_id)
);

alter table if exists delivery_information
 add constraint FK_delivery_information_id_orders
 foreign key (order_id)
 references orders;

alter table if exists shop_information
 add constraint FK_shop_information_id_orders
 foreign key (order_id)
 references orders;

alter table if exists order_item
 add constraint FK_order_item_id_orders
 foreign key (order_id)
 references orders;

alter table if exists order_item_detail
 add constraint FK_order_item_detail_id_order_item
 foreign key (item_order_id, item_product_id)
 references order_item;