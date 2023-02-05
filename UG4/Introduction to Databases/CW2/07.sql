select cast(p.pcode as int), cast(p.price as numeric) from products p join details d on p.pcode=d.pcode group by p.pcode having avg(d.qty) >= 3;
