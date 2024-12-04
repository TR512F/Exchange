CREATE TABLE currencies (
                            id SERIAL PRIMARY KEY,
                            code VARCHAR(3) NOT NULL UNIQUE
);

INSERT INTO currencies (code) VALUES
                                  ('UAH'),
                                  ('USD'),
                                  ('EUR'),
                                  ('GBP');

CREATE TABLE exchange_rates (
                                id SERIAL PRIMARY KEY,
                                from_currency_id INT REFERENCES currencies(id) ON DELETE CASCADE,
                                to_currency_id INT REFERENCES currencies(id) ON DELETE CASCADE,
                                rate NUMERIC(10, 2) NOT NULL,
                                timestamp TIMESTAMP NOT NULL
);

CREATE OR REPLACE FUNCTION generate_random_rate(base_rate NUMERIC) RETURNS NUMERIC AS $$
DECLARE
    random_variation NUMERIC := (RANDOM() * 2 - 1);
BEGIN
    RETURN base_rate + random_variation;
END;
$$ LANGUAGE plpgsql;

DO $$
    DECLARE
        start_date DATE := CURRENT_DATE - INTERVAL '30 days';
        end_date DATE := CURRENT_DATE;
        base_usd_to_uah NUMERIC := 42.00;
        base_eur_to_uah NUMERIC := 45.00;
        base_gbp_to_uah NUMERIC := 52.00;
        my_current_date DATE;
        usd_id INT;
        eur_id INT;
        gbp_id INT;
        uah_id INT;
        eight_hours TIMESTAMP;
        twelve_hours TIMESTAMP;
    BEGIN
        SELECT id INTO usd_id FROM currencies WHERE code = 'USD';
        SELECT id INTO eur_id FROM currencies WHERE code = 'EUR';
        SELECT id INTO gbp_id FROM currencies WHERE code = 'GBP';
        SELECT id INTO uah_id FROM currencies WHERE code = 'UAH';

        my_current_date := start_date;
        WHILE my_current_date <= end_date LOOP
                eight_hours := my_current_date::TIMESTAMP + INTERVAL '8 hours';
                twelve_hours := my_current_date::TIMESTAMP + INTERVAL '12 hours';

                IF eight_hours <= NOW() THEN
                    INSERT INTO exchange_rates (from_currency_id, to_currency_id, rate, timestamp) VALUES
                                                                                                       (usd_id, uah_id, generate_random_rate(base_usd_to_uah), eight_hours),
                                                                                                       (eur_id, uah_id, generate_random_rate(base_eur_to_uah), eight_hours),
                                                                                                       (gbp_id, uah_id, generate_random_rate(base_gbp_to_uah), eight_hours);
                END IF;

                IF twelve_hours <= NOW() THEN
                    INSERT INTO exchange_rates (from_currency_id, to_currency_id, rate, timestamp) VALUES
                                                                                                       (usd_id, uah_id, generate_random_rate(base_usd_to_uah), twelve_hours),
                                                                                                       (eur_id, uah_id, generate_random_rate(base_eur_to_uah), twelve_hours),
                                                                                                       (gbp_id, uah_id, generate_random_rate(base_gbp_to_uah), twelve_hours);
                END IF;

                my_current_date := my_current_date + INTERVAL '1 day';
            END LOOP;
    END $$;