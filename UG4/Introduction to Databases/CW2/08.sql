select cast(t1.custid as int), cast(max(datedifference) as int) as numberofdays from (select c.custid, (min(o1.odate)-o.odate) as datedifference from customers c join orders o on c.custid=o.ocust join orders o1 on (c.custid=o1.ocust and o.odate != o1.odate and o1.odate>o.odate) where c.custid in (select c.custid from customers c join orders o on c.custid=o.ocust group by c.custid having count(o.ordid) > 1 order by c.custid) group by c.custid, o.odate order by c.custid) as t1 group by t1.custid order by t1.custid;


