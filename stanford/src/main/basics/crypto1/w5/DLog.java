package basics.crypto1.w5;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * h=g^x in Zp in |2^40\ space
 * p=13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006084171
 * g=11717829880366207009516117596335367088558084999998952205599979459063929499736583746670572176471460312928594829675428279466566527115212748467589894601965568
 * h=3239475104050450443565264378728065788649097520952449527834792452971981976143292558073856937958553180532878928001494706097394108577585732452307673444020333
 * The trivial algorithm for this problem is to try all 240 possible values of x until the correct one is found, that is until we find an x satisfying h=gx in Zp. This requires 240 multiplications. In this project you will implement an algorithm that runs in time roughly 240=220 using a meet in the middle attack.
 * Let B=2^20. Since x is less than B2 we can write the unknown x base B as x=x0B+x1
 * where x0,x1 are in the range [0,B−1]. Then
 * h=g^x=g^(x0B+x1)=(g^B)^x0⋅gx1   in Zp.
 * By moving the term gx1 to the other side we obtain
 * h/g^x1=(g^B)^x0   in Zp.
 * The variables in this equation are x0,x1 and everything else is known: you are given g,h and B=220.
 * Since the variables x0 and x1 are now on different sides of the equation we can find a solution using meet in the middle (Lecture 3.3 at 14:25):
 * First build a hash table of all possible values of the left hand side h/gx1 for x1=0,1,…,220.
 * Then for each value x0=0,1,2,…,220 check if the right hand side (gB)x0 is in this hash table. If so, then you have found a solution (x0,x1) from which you can compute the required x as x=x0B+x1.
 * The overall work is about 220 multiplications to build the table and another 220 lookups in this table.
 * res= 375374217830
 */

public class DLog {
    //sample numbers. Note we MUST use BigIntegers
    private static final BigInteger H = new BigInteger("3239475104050450443565264378728065788649097520952449527834792452971981976143292558073856937958553180532878928001494706097394108577585732452307673444020333");
    private static final BigInteger G = new BigInteger("11717829880366207009516117596335367088558084999998952205599979459063929499736583746670572176471460312928594829675428279466566527115212748467589894601965568");
    private static final BigInteger P = new BigInteger("13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006084171");
    private static final long B = 1048576;//2^20

    //build hashtable of all possible h/(g^x1) for x1 in 0..B
    private static Map<BigInteger, Long> leftHash() {
        Map<BigInteger, Long> m = new HashMap<>();
        BigInteger n;
        BigInteger gPow;
        BigInteger gInversePow;
        for (long i = 0; i < B; i++) {
            //compute g^x1 mod p
            gPow = G.modPow(new BigInteger(i + ""), P);
            //compute 1/(g^x1) mod p
            gInversePow = gPow.modInverse(P);
            //compute h/(g^x1) mod p
            n = H.multiply(gInversePow);
            n = n.mod(P);
            //store in hashtable
            m.put(n, i);
        }
        System.out.println("Hashtable done");
        return m;
    }

    //compute n = g^B^x0 for x0 in 0..B, then check if n is in hashtable. If it is, we found (x0, x1) and can compute x as x0*B+x1
    private static long computeDiscreteLog(Map<BigInteger, Long> m){
        BigInteger n;
        long res = 0;
        //compute g^B
        BigInteger gB = G.modPow(new BigInteger(B+""), P);
        for(long i=0; i<B; i++){
            //compute g^B^x0
            n = gB.modPow(new BigInteger(i+""), P);
            if(m.containsKey(n)){
                res = i*B+m.get(n);
                break;
            }
        }
        return res;
    }

    public static void main(String [] args){

        Instant start = Instant.now();
        Map<BigInteger, Long> m = leftHash();
        long res = computeDiscreteLog(m);
        System.out.println("Found "+res);
        Instant end = Instant.now();

        System.out.println("Processing time: "+ Duration.between(start, end));
    }
}
