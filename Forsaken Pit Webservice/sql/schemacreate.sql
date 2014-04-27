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
firepower int not null,
health int not null,
max_health int not null,
PRIMARY KEY(id)
);
create index actor__level_depth__x__y on actor (level_depth, x, y);

create table player (
player_key char(10) NOT NULL,
actor_id BIGINT NOT NULL,
points INT NOT NULL,
PRIMARY KEY(player_key)
);
alter table player add constraint player__actor_id__fk foreign key (actor_id) references actor (id);

create table game_event (
id BIGSERIAL,
event_type varchar(10) not null,
message varchar(100) not null,
level_depth int not null,
x int not null,
y int not null,
event_time BIGINT NOT NULL,
source_actor_id BIGINT,
target_actor_id BIGINT,
PRIMARY KEY(id)
);
create index game_event__level_depth__x__y on actor (level_depth, x, y);
alter table game_event add constraint game_event__source_actor_id__fk foreign key (source_actor_id) references actor (id);
alter table game_event add constraint game_event__target_actor_id__fk foreign key (target_actor_id) references actor (id);
