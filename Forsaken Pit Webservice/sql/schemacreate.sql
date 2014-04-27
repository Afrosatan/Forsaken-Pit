create schema fpit;
ALTER SCHEMA fpit OWNER TO postgres;
create role fpit_ws with login;
alter role fpit_ws with password '<password>';

grant select, update, insert, delete on all tables in schema fpit to fpit_ws;
grant usage on all sequences in schema fpit to fpit_ws;
alter default privileges in schema fpit grant select, update, insert, delete on tables to fpit_ws;
alter default privileges in schema fpit grant usage on sequences to fpit_ws;
grant usage on schema fpit to fpit_ws;
alter role fpit_ws set search_path to fpit;

create table actor (
id BIGSERIAL,
actor_type varchar(20) not null,
name varchar(20),
level_depth int not null,
x int not null,
y int not null,
next_action_time BIGINT NOT NULL,
PRIMARY KEY(id)
);
create index actor__level_depth__x__y on actor (x, y);

create table player (
player_key char(10) NOT NULL,
actor_id BIGINT NOT NULL,
PRIMARY KEY(player_key)
);
alter table player add constraint player__actor_id__fk foreign key (actor_id) references actor (id);
