create table users (
    id varchar(10) primary key,
    password varchar(10) not null,
    name varchar(20) not null,
    sex varchar(4) not null,
    age tinyint unsigned not null,
    level tinyint unsigned not null,
    visit smallint unsigned not null,
    cash int unsigned not null
)