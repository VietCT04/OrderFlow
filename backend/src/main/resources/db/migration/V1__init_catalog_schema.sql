-- V1__init_catalog_schema.sql
-- Initial schema for Catalog: category + product

-- 1) Category table
CREATE TABLE category (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Each slug must be unique so we can look up categories by slug
CREATE UNIQUE INDEX ux_category_slug
    ON category(slug);

-- 2) Product table
CREATE TABLE product (
    id          UUID PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    stock       INT NOT NULL,
    image_path  VARCHAR(255),
    category_id UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Business rules
    CONSTRAINT product_price_non_negative CHECK (price >= 0),
    CONSTRAINT product_stock_non_negative CHECK (stock >= 0),

    -- Relation: product → category
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES category(id)
        ON DELETE RESTRICT
);

-- Composite index to support queries:
-- "All products in category X ordered by price"
CREATE INDEX ix_product_category_price
    ON product (category_id, price);
