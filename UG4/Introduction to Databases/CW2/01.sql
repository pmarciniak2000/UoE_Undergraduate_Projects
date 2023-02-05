select cast(c.country as char(3)), cast(count(o.odate) as int) as TotalOrders from customers c join orders o on c.custid=o.ocust where cast(o.odate as varchar) >= cast(2017 as varchar) and cast(o.odate as varchar) < cast(2021 as varchar) group by c.country
union        
select cast(c.country as char(3)), cast(0 as int) as TotalOrders from customers c where c.country not in(select c.country from customers c join orders o on c.custid=o.ocust where cast(o.odate as varchar) >= cast(2017 as varchar) and cast(o.odate as varchar) < cast(2021 as varchar) group by c.country);
