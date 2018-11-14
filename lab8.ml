type 'a bintree = Nil | BT of 'a bintree * 'a * 'a bintree;;
let rec mint = function
| Nil -> 1000
| BT(l,v,r) -> min (mint l) (min v (mint r));;
let rec maxt = function
 | Nil -> 0
 | BT(l,v,r) -> max (maxt l) (max v (maxt r));;
let rec f a v = match a with
 | Nil -> Nil
 | BT(Nil,n,r) when v == n -> BT (Nil, mint r, f r (maxt r))
 | BT(l,n,r) when v == n -> BT (f l (maxt l), maxt l, r)
 | BT(l,n,r) -> if v < n then BT(f l v,n,r) else BT(l, n, f r v);;
 