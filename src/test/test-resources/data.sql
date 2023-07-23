insert into customer(id, name, surname) values (1, 'Jan', 'Kowalski');
insert into customer(id, name, surname) values (2, 'Marcin', 'Nowak');


insert into address(id, customer_id, street, house_number) values (1, 1, 'Postępu', '1');
insert into address(id, customer_id, street, house_number) values (2, 1, 'Postępu', '2');
insert into address(id, customer_id, street, house_number) values (3, 2, 'Postępu', '3');
insert into address(id, customer_id, street, house_number) values (4, 2, 'Postępu', '4');