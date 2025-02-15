-- Create tables
CREATE TABLE nested (
                        id VARCHAR(255) PRIMARY KEY,
                        a_bool BOOLEAN
);

CREATE TABLE resource (
                          id VARCHAR(255) PRIMARY KEY,
                          a_string VARCHAR(255),
                          a_bool BOOLEAN,
                          a_number BIGINT,
                          name VARCHAR(255),
                          created_by VARCHAR(255),
                          nested_id VARCHAR(255) UNIQUE,
                          FOREIGN KEY (nested_id) REFERENCES nested(id) ON DELETE CASCADE
);

CREATE TABLE resource_owned_by (
                                   resource_id VARCHAR(255) NOT NULL,
                                   owned_by VARCHAR(255) NOT NULL,
                                   PRIMARY KEY (resource_id, owned_by),
                                   FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE
);

-- Insert nested data
INSERT INTO nested (id, a_bool) VALUES
                                    ('nested1', true),
                                    ('nested2', false),
                                    ('nested3', true);

-- Insert resource data
INSERT INTO resource (id, a_string, a_bool, a_number, name, created_by, nested_id) VALUES
                                                                                       ('1', 'string', true, 1, 'resource1', '1', 'nested1'),
                                                                                       ('2', 'amIAString?', false, 2, 'resource2', '2', 'nested2'),
                                                                                       ('3', 'anotherString', true, 3, 'resource3', '2', 'nested3');

-- Insert owned_by data
INSERT INTO resource_owned_by (resource_id, owned_by) VALUES
                                                          ('1', '1'),
                                                          ('2', '1'),
                                                          ('3', '2');
