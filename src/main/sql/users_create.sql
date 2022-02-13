create table users (
    id varchar(10) primary key,
    password varchar(10) not null,
    name varchar(20) not null,
    sex varchar(4) not null,
    age int(3) not null
)