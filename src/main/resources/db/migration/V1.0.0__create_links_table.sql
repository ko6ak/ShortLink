create table links (
end_of_short_link varchar(20) primary key,
long_link varchar(500) not null,
ttl timestamp(6));