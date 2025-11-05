create table cart (
    id bigserial primary key,
    buyer_id varchar(255) not null,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    active boolean default true
);
    
create table cart_item (
    cart_id bigint not null,
    product_id bigint not null,
    shop_id varchar(255) not null,
    quantity integer,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    primary key (cart_id, product_id)
);

alter table if exists cart_item 
 add constraint FK_cart_item_id_cart
 foreign key (cart_id) 
 references cart;