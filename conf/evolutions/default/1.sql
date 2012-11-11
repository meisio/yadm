# --- First database schema

# --- !Ups

create table User (
  id                        bigint not null AUTO_INCREMENT,
  firstname                 varchar(255),
  lastname					varchar(255),
  email						varchar(255),
  password                	varchar(255),
  constraint pk_user primary key (id))
;

create table Mail (
  id                        bigint not null AUTO_INCREMENT,
  address                   varchar(255),
  userId					bigint,
  domainId					bigint,
  expires					timestamp,
  constraint pk_mail primary key (id))
;

create table Domain (
  id                        bigint not null AUTO_INCREMENT,
  name		                varchar(255),
  constraint pk_domain primary key (id))
;

alter table Mail add constraint fk_mail_user foreign key (userId) references User (id) on delete restrict on update restrict;
alter table Mail add constraint fk_mail_domain foreign key (domainId) references Domain (id) on delete restrict on update restrict;

# --- !Downs

drop table if exists User;

drop table if exists Mail;

drop table if exists Domain;