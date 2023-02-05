select cast(c.custid as int), c.cname, cast(max(o.odate) as date) as orderdate from customers c join orders o on c.custid=o.ocust group by c.custid order by c.custid;
