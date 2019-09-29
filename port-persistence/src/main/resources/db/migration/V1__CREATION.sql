CREATE SCHEMA DBO;

CREATE TABLE DBO.EVENTS
(
    id       VARCHAR( 256 ),
    alert    BOOLEAN NOT NULL,
    duration INT NOT NULL,
    type     VARCHAR( 256 ),
    host     VARCHAR( 256 )
);

