/* ---------------------------------------------------------------------------
   Copyright (c) 2000 - 2008 by Burkhardt Renz
   $Header: e:/mpt/db/dis-es/db/rcs/azamon-create.sql 3.1 2010/02/04 09:15:07Z br Exp $
	 Encoding utf8 (use :e ++enc=utf8)
   ---------------------------------------------------------------------------
   $Log: azamon-create.sql $
   Revision 3.1  2010/02/04 09:15:07Z  br
   utf8
   Revision 3.0  2008/10/06 07:20:17Z  br Vorbereitung Wintersemester 2008/09
   Revision 1.4  2007/11/18 12:01:03Z  br price numeric, da money deprecated.
   Revision 1.3  2007/09/28 07:32:02Z  br primary key bei orderitems
   Revision 1.2  2006/10/05 10:13:38Z  br Umstellung auf postgreSQL
   Revision 1.1  2005/10/14 05:50:46Z  br Initial revision
   ---------------------------------------------------------------------------
*/

/* Tabelle loeschen
*/
drop table Books cascade;
drop table Customers cascade;
drop table Orders cascade;
drop table Orderitems cascade;


/* Tabellen neu erzeugen */
create table Books
(
  isbn           char(13) primary key,
  title          char(80),
  fname          char(40),
  author         char(40),
  price          numeric(6,2),
  year_published integer
);

create table Customers
(
  cid            integer primary key,
  cname          char(40),
  address        char(200),
  cardnum        char(16)

);

create table Orders
(
  ordernum       integer primary key,
  cid            integer references Customers(cid),
  order_date     date
);

create table Orderitems
(
  ordernum      integer references Orders(ordernum),
  isbn          char(13) references Books(isbn),
  qty           integer,
  primary key( ordernum, isbn )
);

