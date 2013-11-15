/* ---------------------------------------------------------------------------
   Copyright (c) 2000 - 2006 by Burkhardt Renz. All rights reserved.
   $Header: e:/mpt/db/dis-es/db/rcs/azamon-fill.sql 1.5 2010/02/04 09:17:13Z br Exp $
	 Encoding utf8 (use :e ++enc=utf8)
   ---------------------------------------------------------------------------
   $Log: azamon-fill.sql $
   Revision 1.5  2010/02/04 09:17:13Z  br
   utf8
   Revision 1.4  2007/11/18 12:07:46Z  br price jetzt numeric
   Revision 1.2  2006/10/05 10:13:55Z  br Umstellung auf postgreSQL
   Revision 1.1  2005/10/14 05:51:06Z  br Initial revision
   ---------------------------------------------------------------------------
*/


/* alle Tabellen leeren */
delete from Books;
delete from Customers;
delete from Orders;
delete from Orderitems;

/* Tabelle Books fuellen */
insert into Books 
  values( '3-257-21755-2','Schwarzrock', 'Brian', 'Moore', 7.90, 1987 );
insert into Books 
  values( '3-257-21931-8', 'Die Große Viktorianische Sammlung', 'Brian', 'Moore', 8.90, 1990 );
insert into Books 
  values( '3-518-37459-1', 'Der Hauptmann und sein Frauenbataillon', 'Mario', 'Vargas Llosa', 8.80, 1984 );
insert into Books 
  values( '3-518-38020-6', 'Tante Julia und der Kunstschreiber', 'Mario', 'Vargas Llosa', 7.80
, 1988 );
insert into Books 
  values( '3-499-22410-0', 'Geschichte machen', 'Stephen', 'Fry', 9.90, 1999 );
insert into Books 
  values( '3-257-06209-5', 'Entwurf einer Liebe auf den ersten Blick', 'Erich', 'Hackl', 10.90, 1999 );
insert into Books 
  values( '3-422-72318-3', 'Längengrad', 'Dava', 'Zobel', 6.00, 1988 );
insert into Books 
  values( '3-596-13399-8', 'Glück Glanz Ruhm', 'Robert', 'Gernhardt', 5.90, 1997 );
insert into Books 
  values( '3-8031-3112-X', 'Die nackten Masken', 'Luigi', 'Malerba', 23.00, 1995 );
insert into Books 
  values( '3-446-18298-5', 'Erklärt Pereira', 'Antonio', 'Tabucchi',  18.90, 1997 );
insert into Books 
  values( '3-492-24118-2', 'Mit brennender Geduld', 'Antonio',  'Skármeta', 8.00, 2004 );

