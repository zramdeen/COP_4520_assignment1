/*=============================================================================
| Assignment: Calculating prime numbers below N. Synchronized version.
|
| Author: Zahid Ramdeen
| Language: Java
|
| To Compile: (from terminal)
| javac A1Sync.java
|
| To Execute: (from terminal)
| java A1Sync <integer>
|
| Class: COP4520 - Concepts of Parallel and Distributed Processing - Spring 2022
| Instructor: Damian Dechev
| Due Date: 1/28/2022
|
+=============================================================================*/

import java.util.Arrays;

public class A1Sync {
	private static boolean A[];
	private static long sumPrimes = 0;

	public static void main(String[] args) {
		// obtain command line argument from user.
		if(args.length == 0) {
			System.out.println("enter the number to stop at as an argument (eg: java A1 100)");
			System.exit(0);
		}

		// ensure the value entered is an integer and is a valid positive number
		final int MAX = Integer.parseInt(args[0]) + 1;
		assert(MAX > 2);

		// setup the array
		A = new boolean[MAX];
		Arrays.fill(A, true);
		A[0] = false; // not prime by definition
		A[1] = false; // not prime by definition

		// solve the problem
		long start = System.currentTimeMillis();
		int totalPrimes = countPrimes(MAX);
		long finish = System.currentTimeMillis();
		long elapsed = finish - start;
		printAnswer(elapsed, totalPrimes, sumPrimes);

		/*
			to check
			100 				-> 25
			1,000 			-> 168
			10,000			-> 1,229   <--- sqrt of 100 million
			1,000,000 	-> 78,498
			10,000,000	-> 664,579
			100,000,000	-> 5,761,455
		*/
	}

	/**
	 * Prints out the solution in the required format
	 * @param elapsed duration the threaded code took
	 * @param totalPrimes total primes under specified value
	 * @param sumPrimes sum of all primes under specified value
	 */
	private static void printAnswer(long elapsed, int totalPrimes, long sumPrimes){
		System.out.println(((double)elapsed/1000) + "s " + totalPrimes + " " + sumPrimes);
		int i = A.length-1;
		int count = 0;
		while(count < 10){
			count++;
			while(!A[i]){
				i--; // find the first prime
			}
			System.out.println(i);
			i--; // go down one.
		}
	}

	/**
	 * Brute force the computation for primes under sqrt(max).
	 * Use a marking algorithm to mark off all multiples of the primes found.
	 * @param max value to compute primes to
	 * @return count of primes
	 */
	private static int countPrimes(int max){
		int sqrt = (int) Math.sqrt(max) + 1;

		// brute force all values under sqrt of n..
		for (int i = 2; i < sqrt; i++) {
			if(isPrime(i)){
				A[i] = true;
			} else {
				A[i] = false;
			}
		}

		// mark all values now..
		for (int i = 2; i < sqrt; i++) {
			if(A[i]){
				int squared = i*i;
				int j = 0;
				int next = squared + j*i;
				while (next < max){
					A[next] = false;
					j++;
					next = squared + j*i;
				}
			}
		}

		// count and sum all values now
		int count = 0;
		for (int i = 2; i < max; i++) {
			if(A[i]){
				count++;
				sumPrimes += i;
			}
		}


		return count;
	}

	/**
	 * Divisibility check for prime
	 * @param v number to check
	 * @return whether the number is prime
	 */
	private static boolean isPrime(int v){
		if(v <= 2) return true;

		int sqrt = (int) Math.sqrt(v);
		for (int i = 2; i <= sqrt; i++) {
			if(v % i == 0) return false;
		}
		return true;
	}
}
