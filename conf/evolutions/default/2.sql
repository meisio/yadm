# --- !Downs

DELETE * FROM domain

# --- !Ups

insert into domain (id,name) values (  1,'localhost');
insert into domain (id,name) values (  2,'test1');
insert into domain (id,name) values (  3,'test2');
