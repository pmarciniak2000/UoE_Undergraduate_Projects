select cast(c.custid as int), cast(sum(d.qty) as int) as TotalBooks from orders o join details d on o.ordid=d.ordid join products p on d.pcode = p.pcode join customers c on c.custid=o.ocust where p.ptype = 'BOOK' and c.custid IN (select c.custid from orders o join details d on o.ordid=d.ordid join products p on d.pcode = p.pcode join customers c on c.custid=o.ocust group by c.custid having sum(CASE WHEN p.ptype = 'BOOK' THEN d.qty END) > 2*(sum(CASE WHEN p.ptype = 'MUSIC' THEN d.qty END)+sum(CASE WHEN p.ptype = 'MOVIE' THEN d.qty END))) group by c.custid order by c.custid;



