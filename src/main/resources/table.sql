CREATE TABLE trade_strategy
(
    idx                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    symbol             INT    NOT NULL,
    interval           INT    NOT NULL,
    order_strategy_idx BIGINT NOT NULL,
    buy_strategy_idx   BIGINT NOT NULL,
    CONSTRAINT fk_order_strategy FOREIGN KEY (order_strategy_idx) REFERENCES order_strategy (idx),
    CONSTRAINT fk_buy_strategy FOREIGN KEY (buy_strategy_idx) REFERENCES buy_strategy (idx)
);

CREATE TABLE order_strategy
(
    idx          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type         int          NOT NULL,
    target_value varchar(500) NOT NULL
);

CREATE TABLE buy_strategy
(
    idx                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type                int          NOT NULL,
    target_value        varchar(500) NOT NULL,
    leverage            int          NOT NULL,
    order_balance_ratio double(4, 3
) NOT NULL
);

create table trade_log
(
    idx               bigint auto_increment
        primary key,
    symbol            int                                 not null,
    side              int                                 not null,
    order_strategy_type int                                 not null,
    buy_strategy_type int                                 not null,
    order_state       int                                 not null,
    avg_price         double(15, 7)                       not null,
    position_quantity double(15, 7)                       not null,
    close_price       double(15, 7)                       null,
    pnl               double(15, 7)                       null,
    `option`          varchar(500)                        null,
    update_date       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    created_date      timestamp default CURRENT_TIMESTAMP not null
);

