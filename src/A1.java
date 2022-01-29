/*=============================================================================
| Assignment: Calculating prime numbers below N. Parallel version.
|
| Author: Zahid Ramdeen
| Language: Java
|
| To Compile: (from terminal)
| javac A1.java
|
| To Execute: (from terminal)
| java A1 <integer>
|
| Class: COP4520 - Concepts of Parallel and Distributed Processing - Spring 2022
| Instructor: Damian Dechev
| Due Date: 1/28/2022
|
+=============================================================================*/

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class A1 {
	// variables for information on primes
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
		int totalPrimes = countPrimes(MAX); // threaded code in here
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
	 * Using 8 concurrent threads compute the primes below "max".
	 * Also sets the global variable sumPrimes.
	 * @param max value to compute primes to
	 * @return count of primes
	 */
	private static int countPrimes(int max){
		// synchronized counter... starts at 2 to prevent the algorithm from overwriting 0,1 as true.
		Counter c = new Counter(2);

		// create 8 threads
		int totalThreads = 8;
		Thread tarr[] = new Thread[totalThreads];
		for (int i = 0; i < totalThreads; i++) {
			tarr[i] = new Thread(new Worker(c, max), "t" + i);
		}

		// start threads
		for (Thread t : tarr) {
			t.start();
		}

		// force main thread to wait for child threads to finish
		try {
			for (Thread t : tarr) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// enqueue the primes found below sqrt(max)
		int sqrtMax = (int) Math.sqrt(max);
		BlockingQueue<Integer> bq = new ArrayBlockingQueue<Integer>(sqrtMax);
		for (int i = 2; i < sqrtMax; i++) {
			// find a prime
			if (A[i]) {
				bq.add(i); // add to the queue
			}
		}

		// dequeue primes found, get threads to mark off all multiples
		for (int i = 0; i < totalThreads; i++) {
			tarr[i] = new Thread(new MarkingWorker(bq, max), "t" + i);
		}

		// start threads
		for (Thread t : tarr) {
			t.start();
		}

		// force main thread to wait for child threads to finish
		try {
			for (Thread t : tarr) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// get total
		int count = 0;
		int index = 0;
		for (boolean a:A) {
			if(a) {
				count++;
				sumPrimes += index;
			}
			index++;
		}

		// return the count of primes
		return count;
	}

	/**
	 * Brute force thread for finding primes below the square root of a specific number.
	 */
	static class Worker implements Runnable {
		private final Counter c;
		private final int MAX;
		private final int SQRT_MAX;

		public Worker(Counter count, int max){
			this.c = count;
			this.MAX = max;
			this.SQRT_MAX = (int) Math.sqrt(max);
		}

		/**
		 * Gets a value from the Atomic Counter and checks if it's prime.
		 */
		@Override
		public void run() {
			int cur = c.getAndIncrement();
			while(cur <= SQRT_MAX){
				// check if the current value is prime
				if(isPrime(cur)){
					A[cur] = true;
				} else {
					A[cur] = false;
				}

				// get next work.
				cur = c.getAndIncrement();
			}
		}

		/**
		 * Divisibility check for prime
		 * @param v number to check
		 * @return whether the number is prime
		 */
		private boolean isPrime(int v){
			if(v <= 2) return true;

			int sqrt = (int) Math.sqrt(v);
			for (int i = 2; i <= sqrt; i++) {
				if(v % i == 0) return false;
			}
			return true;
		}
	}

	/**
	 * Thread for marking off primes using a blocking queue.
	 * Each thread obtains a prime number and marks off multiples in parallel.
	 */
	static class MarkingWorker implements Runnable {
		private BlockingQueue<Integer> bq = null;
		private final int max;

		public MarkingWorker(BlockingQueue bq, int max){
			this.bq = bq;
			this.max = max;
		}

		/**
		 * Obtains a prime (integer) from the blocking queue.
		 * Marks off all multiples of the number.
		 * Exits when queue is empty.
		 */
		@Override
		public void run() {
			while(true){
				Integer i = bq.poll();

				// no work left to do.
				if(null == i){
					return;
				}

				// mark off all multiples
				int squared = i*i;
				int j = 0;
				int next = squared + (j*i);
				while(next < max){
					A[next] = false;
					j++;
					next = squared + (j*i);
				}
			}

		}
	}

	/**
	 * Atomic Counter used for synchronizing threads in the brute-force portion of the algorithm
	 */
	static class Counter {
		private AtomicInteger count;

		public Counter(int initial){
			count = new AtomicInteger(initial);
		}

		/**
		 * Atomic increment operation.
		 * @return value before increment.
		 */
		public int getAndIncrement(){
			return count.getAndIncrement();
		}

		/**
		 * Atomic get
		 * @return current value
		 */
		public int getValue(){
			return count.get();
		}
	}
}
