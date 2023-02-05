select cast(i.invid as int), cast(sum(p.amount)-i.amount as numeric) as RefundAmount from invoices i join payments p on i.invid=p.invid group by i.invid having sum(p.amount)-i.amount > 0;
