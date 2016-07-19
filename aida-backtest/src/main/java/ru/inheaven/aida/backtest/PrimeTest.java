package ru.inheaven.aida.backtest;

import java.util.stream.IntStream;

/**
 * @author inheaven on 07.07.2016.
 */
public class PrimeTest {
    public static void main(String... args){
        IntStream.range(0, 1001).filter(PrimeTest::isPrime).forEach(System.out::println);
    }

    static boolean isPrime(int n) {
        //check if n is a multiple of 2
        if (n%2==0) return false;
        //if not, then just check the odds
        for(int i=3;i*i<=n;i+=2) {
            if(n%i==0)
                return false;
        }
        return true;
    }
}
