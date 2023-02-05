select cast(p.pcode as int), cast(sum(d.qty) as int) as TimesOrdered from products p join details d on p.pcode=d.pcode where p.pcode in(select p.pcode from products p join products p1 on(p.pcode != p1.pcode and p.ptype=p1.ptype) join details d on p.pcode=d.pcode join details d1 on p1.pcode=d1.pcode group by p.pcode having avg(d.qty) >= 1.5*avg(case when (p.pcode != p1.pcode) then d1.qty end)) group by p.pcode;






